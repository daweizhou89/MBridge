package com.github.daweizhou89.mbridge.compiler;

import com.google.auto.service.AutoService;
import com.github.daweizhou89.mbridge.annotation.Autowired;
import com.github.daweizhou89.mbridge.annotation.Action;
import com.github.daweizhou89.mbridge.annotation.MBridgeService;
import com.github.daweizhou89.mbridge.annotation.MBridgeDestination;
import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

@AutoService(Processor.class)
@SupportedOptions(Consts.KEY_MODULE_NAME)
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class MBridgeCompiler extends AbstractProcessor {

    private Types types;
    private Elements elementUtils;
    private TypeUtils typeUtils;
    private String moduleName = null; // Module name, maybe its 'app' or others

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        elementUtils = processingEnv.getElementUtils();
        types = processingEnv.getTypeUtils();            // Get type utils.

        typeUtils = new TypeUtils(types, elementUtils);

        // Attempt to get user configuration [moduleName]
        Map<String, String> options = processingEnv.getOptions();
        if (options != null && !options.isEmpty()) {
            moduleName = options.get(Consts.KEY_MODULE_NAME);
        }
        if (moduleName == null || "".equals(moduleName)) {
            moduleName = String.valueOf(new Random().nextInt(Integer.MAX_VALUE));
        } else {
            moduleName = moduleName.replace('-', '_');
        }
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> supportedAnnotationTypes = new LinkedHashSet<>();
        supportedAnnotationTypes.add(MBridgeDestination.class.getCanonicalName());
        supportedAnnotationTypes.add(Autowired.class.getCanonicalName());
        supportedAnnotationTypes.add(MBridgeService.class.getCanonicalName());
        supportedAnnotationTypes.add(Action.class.getCanonicalName());
        return supportedAnnotationTypes;
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (annotations == null || annotations.isEmpty()) {
            return false;
        }
        Set<? extends Element> intentTargetElements = roundEnv.getElementsAnnotatedWith(MBridgeDestination.class);

        List<ClassName[]> allInjectHelper = new ArrayList<>();
        List<Pair<String, ClassName>> allPath = new ArrayList<>();
        for (Element element : intentTargetElements) {
            try {
                processTypeElement(roundEnv, element, allInjectHelper, allPath);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        ClassName intentActions = null;
        try {
            Set<? extends Element> intentActionsElements = roundEnv.getElementsAnnotatedWith(MBridgeService.class);
            intentActions = createIntentActions(roundEnv, intentActionsElements);
        } catch (Exception e) {
            e.printStackTrace();
        }

        createModuleHelper(roundEnv, allInjectHelper, allPath, intentActions);

        return true;
    }

    private void createModuleHelper(RoundEnvironment roundEnv, List<ClassName[]> allInjectHelper, List<Pair<String, ClassName>> allPath, ClassName intentActions) {

        // inject
        MethodSpec.Builder injectBuilder = MethodSpec.methodBuilder("inject")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .returns(TypeName.BOOLEAN)
                .addParameter(Consts.CLASS_NAME_OBJECT, "target")
                .addStatement("String className = target.getClass().getName()");
        if (!allInjectHelper.isEmpty()) {
            injectBuilder.beginControlFlow("switch (className)");
            for (ClassName[] classNames : allInjectHelper) {
                ClassName target = classNames[0];
                injectBuilder.beginControlFlow("case $S:", target.toString());
                injectBuilder.addStatement("$T.inject(target)", classNames[1]);
                injectBuilder.endControlFlow();
                injectBuilder.addStatement("return true");
            }
            injectBuilder.endControlFlow();
        }
        injectBuilder.addStatement("return false");

        // prepare
        MethodSpec.Builder prepareBuilder = MethodSpec.methodBuilder("prepare")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .returns(TypeName.INT)
                .addParameter(Consts.CLASS_NAME_INTENT, "intent")
                .addParameter(Consts.CLASS_NAME_CONTEXT, "substitute")
                .addParameter(String.class, "path");
        if (!allPath.isEmpty()) {
            prepareBuilder.beginControlFlow("switch (path)");

            for (Pair<String, ClassName> classNames : allPath) {
                prepareBuilder.beginControlFlow("case $S:", classNames.first);
                prepareBuilder.addStatement("intent.setClass(substitute, $T.class)", classNames.second);
                prepareBuilder.endControlFlow();
                prepareBuilder.addStatement("return $T.getType($T.class)", Consts.CLASS_NAME_DESTINATION_WRAPPER, classNames.second);
            }
            prepareBuilder.endControlFlow();
        }
        prepareBuilder.addStatement("return $T.getType(null)", Consts.CLASS_NAME_DESTINATION_WRAPPER);

        MethodSpec.Builder getModuleNameBuilder = MethodSpec.methodBuilder("getModuleName")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .returns(Consts.CLASS_NAME_STRING)
                .addStatement("return $S", moduleName);

        MethodSpec.Builder getIntentActionsClassBuilder = MethodSpec.methodBuilder("getMBridgeServiceClass")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .returns(Class.class);
        if (intentActions != null) {
            getIntentActionsClassBuilder.addStatement("return $T.class", intentActions);
        } else {
            getIntentActionsClassBuilder.addStatement("return null");
        }

        List<MethodSpec> methodSpecs = new ArrayList<>();
        methodSpecs.add(getModuleNameBuilder.build());
        methodSpecs.add(getIntentActionsClassBuilder.build());
        methodSpecs.add(injectBuilder.build());
        methodSpecs.add(prepareBuilder.build());

        ClassName classNameBase = ClassName.get(Consts.APT_CORE_PACKAGENAME, Consts.CLASS_SIMPLE_NAME_IMBRIDGE);
        String bridgeTypeName = Consts.CLASS_PREFIX_MBRIDGE + moduleName;
        TypeSpec bridgeTypeSpec = TypeSpec.classBuilder(bridgeTypeName)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addSuperinterface(classNameBase)
                .addMethods(methodSpecs)
                .build();

        writeNewFile(Consts.APT_CORE_PACKAGENAME, bridgeTypeSpec);
    }

    private void processTypeElement(RoundEnvironment roundEnv, Element element, List<ClassName[]> allInjectHelper, List<Pair<String, ClassName>> allPath) {
        // Just Process TypeElement
        if (!(element instanceof TypeElement)) {
            return;
        }
        TypeElement typeElement = (TypeElement) element;
        final String packageName = elementUtils.getPackageOf(typeElement).getQualifiedName().toString();

        // Get Path
        MBridgeDestination destination = typeElement.getAnnotation(MBridgeDestination.class);
        boolean hasPath = false;
        if (!"".equals(destination.path())) {
            allPath.add(new Pair<>(destination.path(), ClassName.get(typeElement)));
            hasPath = true;
        }

        // Get All Members
        List<? extends Element> members = elementUtils.getAllMembers(typeElement);

        String bridgeTypeName = element.getSimpleName().toString() + Consts.CLASS_SUFFIX_MBRIDGE;
        List<MethodSpec> bridgeMethodList = new ArrayList<>();
        MethodSpec.Builder injectBuilder = MethodSpec.methodBuilder("inject")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(Consts.CLASS_NAME_OBJECT, "target");
        injectBuilder.addStatement("$T substitute = ($T)target", ClassName.get(typeElement), ClassName.get(typeElement));
        injectBuilder.addStatement("$T extras = null", Consts.CLASS_NAME_BUNDLE);

        injectBuilder.beginControlFlow("if (target instanceof $T && (($T)target).getIntent() != null)", Consts.CLASS_NAME_ACTIVITY, Consts.CLASS_NAME_ACTIVITY);
        injectBuilder.addStatement("$T intent = (($T)target).getIntent()", Consts.CLASS_NAME_INTENT, Consts.CLASS_NAME_ACTIVITY);
        injectBuilder.addStatement("extras = intent.getExtras()");

        MethodSpec.Builder injectOfDataBuilder = null;
        if (hasPath) {
            injectBuilder.beginControlFlow("if (intent == null || injectOfData(substitute, intent))");
            injectBuilder.addStatement("return");
            injectBuilder.endControlFlow();

            injectOfDataBuilder = MethodSpec.methodBuilder("injectOfData")
                    .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
                    .returns(TypeName.BOOLEAN)
                    .addParameter(ClassName.get(typeElement), "substitute")
                    .addParameter(Consts.CLASS_NAME_INTENT, "intent")
                    .addStatement("$T uri = intent.getData()", Consts.CLASS_NAME_URI)
                    .beginControlFlow("if (uri == null)")
                    .addStatement("return false")
                    .endControlFlow()
                    .addStatement("String param = null");
        }

        injectBuilder.nextControlFlow("else if (target instanceof $T)", Consts.CLASS_NAME_FRAGMENT_V4);
        injectBuilder.addStatement("extras = (($T)target).getArguments()", Consts.CLASS_NAME_FRAGMENT_V4);
        injectBuilder.endControlFlow();

        injectBuilder.beginControlFlow("if (extras != null)")
                .addStatement("inject(substitute, extras)")
                .endControlFlow();

        MethodSpec.Builder injectOfIntentBuilder = MethodSpec.methodBuilder("inject")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(ClassName.get(typeElement), "substitute")
                .addParameter(Consts.CLASS_NAME_BUNDLE, "extras");

        String wrapperTypeName = Consts.CLASS_SIMPLE_NAME_DESTINATION_WRAPPER;
        ClassName wrapperClassName = ClassName.get(packageName, bridgeTypeName + "." + wrapperTypeName);
        ClassName baseWrapperClassName = ClassName.get(Consts.APT_CORE_PACKAGENAME, wrapperTypeName);
        List<MethodSpec> wrapperMethodList = new ArrayList<>();
        int injectCount = 0;
        for (Element member : members) {
            final boolean isVariableElement = member instanceof VariableElement;
            if (!isVariableElement) {
                continue;
            }
            Autowired autowired = member.getAnnotation(Autowired.class);
            if (autowired == null) {
                continue;
            }

            final String extraKey;
            if ("".equals(autowired.name())) {
                extraKey = member.getSimpleName().toString();
            } else {
                extraKey = autowired.name();
            }
            final String defaultValue = autowired.defaultValue();
            buildGetStatement(injectOfIntentBuilder, member, defaultValue, extraKey);
            if (injectOfDataBuilder != null) {
                final String uriKey;
                if ("".equals(autowired.uriName())) {
                    uriKey = extraKey;
                } else {
                    uriKey = autowired.uriName();
                }
                buildUriGetStatement(injectOfDataBuilder, member, defaultValue, uriKey);
            }

            MethodSpec.Builder extraBuilder = MethodSpec.methodBuilder(extraKey)
                    .addModifiers(Modifier.PUBLIC)
                    .returns(wrapperClassName)
                    .addParameter(ClassName.get(member.asType()), "value");

            buildPutStatement(extraBuilder, member, extraKey);
            extraBuilder.addStatement("return this");
            wrapperMethodList.add(extraBuilder.build());
            ++injectCount;
        }

        // bridge
        if (injectCount > 0) {
            bridgeMethodList.add(injectBuilder.build());
            bridgeMethodList.add(injectOfIntentBuilder.build());
            if (injectOfDataBuilder != null) {
                injectOfDataBuilder.addStatement("return true");
                bridgeMethodList.add(injectOfDataBuilder.build());
            }
            allInjectHelper.add(new ClassName[]{ClassName.get(typeElement), ClassName.get(packageName, bridgeTypeName)});
        }
        MethodSpec.Builder intentBuilder = MethodSpec.methodBuilder("intent")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(wrapperClassName)
                .addStatement("return new $T()", wrapperClassName);
        bridgeMethodList.add(intentBuilder.build());

        // wrapper
        MethodSpec.Builder startBuilder = MethodSpec.methodBuilder("prepare")
                .addModifiers(Modifier.PROTECTED)
                .addAnnotation(Override.class)
                .addParameter(Consts.CLASS_NAME_CONTEXT, "substitute")
                .addStatement("intent.setClass(substitute, $T.class)", ClassName.get(typeElement))
                .addStatement("type = $T.getType($T.class)", Consts.CLASS_NAME_DESTINATION_WRAPPER, ClassName.get(typeElement));
        wrapperMethodList.add(startBuilder.build());

        // Create New Type
        TypeSpec wrapperTypeSpec = TypeSpec.classBuilder(wrapperTypeName)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .superclass(baseWrapperClassName)
                .addMethods(wrapperMethodList)
                .build();
        TypeSpec bridgeTypeSpec = TypeSpec.classBuilder(bridgeTypeName)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addMethods(bridgeMethodList)
                .addType(wrapperTypeSpec)
                .build();

        // Write New File
        writeNewFile(packageName, bridgeTypeSpec);
    }

    private void writeNewFile(String packageName, TypeSpec typeSpec) {
        JavaFile javaFile = JavaFile.builder(packageName, typeSpec).build();
        System.err.println(packageName + "-" + typeSpec.name);
        try {
            javaFile.writeTo(processingEnv.getFiler());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void buildPutStatement(MethodSpec.Builder builder, Element member, String extraKey) {
        StringBuilder statement = new StringBuilder();
        statement.append("intent.");
        boolean addStatement = true;
        int type = typeUtils.typeExchange(member);
        if (type == TypeKind.BOOLEAN.ordinal()
                || type == TypeKind.BYTE.ordinal()
                || type == TypeKind.SHORT.ordinal()
                || type == TypeKind.INT.ordinal()
                || type == TypeKind.LONG.ordinal()
                || type == TypeKind.FLOAT.ordinal()
                || type == TypeKind.DOUBLE.ordinal()
                || type == TypeKind.STRING.ordinal()
                || type == TypeKind.CHARSEQUENCE.ordinal()
                || type == TypeKind.PARCELABLE.ordinal()
                || type == TypeKind.SERIALIZABLE.ordinal()
                ) {
            statement.append("putExtra(\"")
                    .append(extraKey)
                    .append("\", value)");
        } else {
            builder.addStatement("$T.putExtra(intent, $S, value)", Consts.CLASS_NAME_GSON_UTILS, extraKey);
            addStatement = false;
        }
        if (addStatement) {
            builder.addStatement(statement.toString(), extraKey);
        }
    }

    private void buildUriGetStatement(MethodSpec.Builder builder, Element member, String defaultValue, String extraKey) {
        final String memberName = member.getSimpleName().toString();
        String typeName = member.asType().toString();
        boolean addStatement = true;
        StringBuilder statement = new StringBuilder();
        statement.append("substitute.").append(memberName).append(" = $T.");
        int type = typeUtils.typeExchange(member);
        if (type == TypeKind.BOOLEAN.ordinal()) {
            statement.append("getBooleanQueryParameter(uri, $S, ")
                    .append(getDefalutValue(type, defaultValue))
                    .append(")");
        } else if (type == TypeKind.SHORT.ordinal()) {
            statement.append("getShortQueryParameter(uri, $S, (short) ")
                    .append(getDefalutValue(type, defaultValue))
                    .append(")");
        } else if (type == TypeKind.INT.ordinal()) {
            statement.append("getIntQueryParameter(uri, $S, ")
                    .append(getDefalutValue(type, defaultValue))
                    .append(")");
        } else if (type == TypeKind.LONG.ordinal()) {
            statement.append("getLongQueryParameter(uri, $S, ")
                    .append(getDefalutValue(type, defaultValue))
                    .append(")");
        } else if (type == TypeKind.FLOAT.ordinal()) {
            statement.append("getFloatQueryParameter(uri, $S, ")
                    .append(getDefalutValue(type, defaultValue))
                    .append(")");
        } else if (type == TypeKind.DOUBLE.ordinal()) {
            statement.append("getDoubleQueryParameter(uri, $S, ")
                    .append(getDefalutValue(type, defaultValue))
                    .append(")");
        } else if (type == TypeKind.STRING.ordinal() || type == TypeKind.CHARSEQUENCE.ordinal()) {
            statement.append("getStringQueryParameter(uri, $S)");
        } else {
            statement = new StringBuilder();
            statement.append("substitute.").append(memberName).append(" = $T.getParam(uri, $S, new $T<$T>() {}.getType())");
            builder.addStatement(statement.toString(), Consts.CLASS_NAME_GSON_UTILS, extraKey, Consts.CLASS_NAME_TYPE_TOKEN, ClassName.get(member.asType()));
            addStatement = false;
        }
        if (addStatement) {
            builder.addStatement(statement.toString(), Consts.CLASS_NAME_URI_UTILS, extraKey);
        }
    }

    private void buildGetStatement(MethodSpec.Builder builder, Element member, String defaultValue, String extraKey) {
        final String memberName = member.getSimpleName().toString();
        String typeName = member.asType().toString();
        boolean addStatement = true;
        StringBuilder statement = new StringBuilder();
        statement.append("substitute.").append(memberName).append(" = extras.");
        int type = typeUtils.typeExchange(member);
        if (type == TypeKind.BOOLEAN.ordinal()) {
            statement.append("getBoolean($S, ")
                    .append(getDefalutValue(type, defaultValue))
                    .append(")");
        } else if (type == TypeKind.BYTE.ordinal()) {
            statement.append("getByte($S, (byte) ")
                    .append(getDefalutValue(type, defaultValue))
                    .append(")");
        } else if (type == TypeKind.SHORT.ordinal()) {
            statement.append("getShort($S, (short) ")
                    .append(getDefalutValue(type, defaultValue))
                    .append(")");
        } else if (type == TypeKind.INT.ordinal()) {
            statement.append("getInt($S, ")
                    .append(getDefalutValue(type, defaultValue))
                    .append(")");
        } else if (type == TypeKind.LONG.ordinal()) {
            statement.append("getLong($S, ")
                    .append(getDefalutValue(type, defaultValue))
                    .append(")");
        } else if (type == TypeKind.FLOAT.ordinal()) {
            statement.append("getFloat($S, ")
                    .append(getDefalutValue(type, defaultValue))
                    .append(")");
        } else if (type == TypeKind.DOUBLE.ordinal()) {
            statement.append("getDouble($S, ")
                    .append(getDefalutValue(type, defaultValue))
                    .append(")");
        } else if (type == TypeKind.STRING.ordinal()) {
            statement.append("getString($S)");
        } else if (type == TypeKind.CHARSEQUENCE.ordinal()) {
            statement.append("getCharSequence($S)");
        } else if (type == TypeKind.PARCELABLE.ordinal()) {
            statement.append("getParcelable($S)");
        } else if (type == TypeKind.SERIALIZABLE.ordinal()) {
            statement = new StringBuilder();
            statement.append("substitute.")
                    .append(memberName)
                    .append(" = (")
                    .append(typeName)
                    .append(") extras.")
                    .append("getSerializable($S)");
        } else {
            statement = new StringBuilder();
            statement.append("substitute.").append(memberName).append(" = $T.getExtra(extras, $S, new $T<$T>() {}.getType())");
            builder.addStatement(statement.toString(), Consts.CLASS_NAME_GSON_UTILS, extraKey, Consts.CLASS_NAME_TYPE_TOKEN, ClassName.get(member.asType()));
            addStatement = false;
        }
        if (addStatement) {
            builder.addStatement(statement.toString(), extraKey);
        }
    }

    private String getDefalutValue(int type, String defaultValue) {
        if (defaultValue != null && !"".equals(defaultValue)) {
            return defaultValue;
        }
        if (type == TypeKind.BOOLEAN.ordinal()) {
            defaultValue = "false";
        } else if (type == TypeKind.BYTE.ordinal()) {
            defaultValue = "0";
        } else if (type == TypeKind.SHORT.ordinal()) {
            defaultValue = "0";
        } else if (type == TypeKind.INT.ordinal()) {
            defaultValue = "0";
        } else if (type == TypeKind.LONG.ordinal()) {
            defaultValue = "0";
        } else if (type == TypeKind.FLOAT.ordinal()) {
            defaultValue = "0";
        } else if (type == TypeKind.DOUBLE.ordinal()) {
            defaultValue = "0";
        }
        return defaultValue;
    }

    private ClassName createIntentActions(RoundEnvironment roundEnv, Set<? extends Element> intentActionsElements) {

        if (intentActionsElements == null || intentActionsElements.isEmpty()) {
            return null;
        }

        // invoke
        MethodSpec.Builder invokeBuilder = MethodSpec.methodBuilder("invoke")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .returns(TypeName.OBJECT)
                .addParameter(Consts.CLASS_NAME_STRING, "action")
                .addParameter(Consts.CLASS_NAME_TYPE, "type")
                .addParameter(ArrayTypeName.of(Consts.CLASS_NAME_OBJECT), "params");

        invokeBuilder.addStatement("Object value = null");

        if (!intentActionsElements.isEmpty()) {
            invokeBuilder.beginControlFlow("switch (action)");
        }

        List<FieldSpec> fieldSpecs = new ArrayList<>();
        for (Element element : intentActionsElements) {
            try {
                processTypeElementIntentActions(roundEnv, element, fieldSpecs, invokeBuilder);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (!intentActionsElements.isEmpty()) {
            invokeBuilder.beginControlFlow("default:");
            StringBuilder statement = new StringBuilder();
            statement.append("throw new $T(\"Action \" + action + \" is not found in Module ");
            statement.append(moduleName);
            statement.append("\")");
            invokeBuilder.addStatement(statement.toString(), Consts.CLASS_NAME_RUNTIME_EXCEPTION);
            invokeBuilder.endControlFlow();
            invokeBuilder.endControlFlow();
        }

        invokeBuilder.addStatement("return value");

        ClassName classNameBase = ClassName.get(Consts.APT_CORE_PACKAGENAME, Consts.CLASS_SIMPLE_NAME_IMBRIDGE_SERVICE);
        String intentActionsTypeName = Consts.CLASS_PREFIX_MBRIDGE_SERVICE + moduleName;
        TypeSpec actionsTypeSpec = TypeSpec.classBuilder(intentActionsTypeName)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addSuperinterface(classNameBase)
                .addFields(fieldSpecs)
                .addMethod(invokeBuilder.build())
                .build();

        writeNewFile(Consts.APT_CORE_PACKAGENAME, actionsTypeSpec);

        return ClassName.get(Consts.APT_CORE_PACKAGENAME, intentActionsTypeName);
    }

    private void processTypeElementIntentActions(RoundEnvironment roundEnv, Element element, List<FieldSpec> fieldSpecs, MethodSpec.Builder invokeBuilder) {
        // Just Process TypeElement
        if (!(element instanceof TypeElement)) {
            return;
        }

        TypeElement typeElement = (TypeElement) element;
        final String fieldTypeName = typeElement.getSimpleName().toString();
        final String fieldName = "m" + fieldTypeName;
        TypeName typeName = TypeName.get(typeElement.asType());
        FieldSpec fieldSpec = FieldSpec.builder(typeName, fieldName, Modifier.PUBLIC, Modifier.FINAL)
                .initializer("new $T()", typeName)
                .build();
        fieldSpecs.add(fieldSpec);

        // Get All Members
        List<? extends Element> members = elementUtils.getAllMembers(typeElement);

        for (Element member : members) {
            final boolean isExecutableElement = member instanceof ExecutableElement;
            if (!isExecutableElement) {
                continue;
            }
            Action action = member.getAnnotation(Action.class);
            if (action == null) {
                continue;
            }

            final String actionName = action.action();
            final boolean rawParams = action.rawParams();
            invokeBuilder.beginControlFlow("case $S:", actionName);
            ExecutableElement executableElement = (ExecutableElement) member;
            List<? extends VariableElement> parameters = executableElement.getParameters();
            addActionStatement(invokeBuilder, fieldName, actionName, rawParams, TypeName.get(executableElement.getReturnType()), parameters);
            invokeBuilder.addStatement("break");
            invokeBuilder.endControlFlow();
        }
    }

    private void addActionStatement(MethodSpec.Builder invokeBuilder, String fieldName, String actionName, boolean rawParams, TypeName returnType, List<? extends VariableElement> parameters) {
        StringBuilder stringBuilder = new StringBuilder();
        if (!TypeName.VOID.equals(returnType)) {
            if (rawParams) {
                stringBuilder.append("value = ");
            } else {
                stringBuilder.append("Object oValue = ");
            }
        }
        stringBuilder.append(fieldName).append(".").append(actionName).append("(");
        if (parameters != null && !parameters.isEmpty()) {
            String[] paramAppendArray = new String[parameters.size()];
            for (int i = 0; i < parameters.size(); i++) {
                VariableElement parameter = parameters.get(i);
                String paramAppend;
                if (!rawParams && typeUtils.isObjectType(parameter)) {
                    TypeName paramType = ClassName.get(parameter.asType());
                    StringBuilder subStringBuilder = new StringBuilder()
                            .append("$T param")
                            .append(i)
                            .append(" = ")
                            .append(Consts.CLASS_NAME_GSON_UTILS.toString())
                            .append(".getValue(params[")
                            .append(i)
                            .append("], new com.google.gson.reflect.TypeToken<$T>() {}.getType()")
                            .append(")");
                    invokeBuilder.addStatement(subStringBuilder.toString(), paramType, paramType);
                    paramAppend = "param" + i;
                } else {
                    paramAppend = "(" + ClassName.get(parameter.asType()).toString() + ") params[" + i + "]";
                }
                paramAppendArray[i] = paramAppend;
            }
            for (int i = 0; i < paramAppendArray.length; i++) {
                if (i != 0) {
                    stringBuilder.append(", ");
                }
                stringBuilder.append(paramAppendArray[i]);
            }
        }
        stringBuilder.append(")");
        invokeBuilder.addStatement(stringBuilder.toString());
        if (!TypeName.VOID.equals(returnType) && !rawParams) {
            invokeBuilder.beginControlFlow("if (type != null)");
            invokeBuilder.addStatement("value = $T.getValue(oValue, type)", Consts.CLASS_NAME_GSON_UTILS);
            invokeBuilder.endControlFlow();
        }
    }
}

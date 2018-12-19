package com.github.daweizhou89.mbridge.compiler;

import javax.lang.model.element.Element;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import static com.github.daweizhou89.mbridge.compiler.Consts.ACTIVITY;
import static com.github.daweizhou89.mbridge.compiler.Consts.BOOLEAN;
import static com.github.daweizhou89.mbridge.compiler.Consts.BUNDLE;
import static com.github.daweizhou89.mbridge.compiler.Consts.BYTE;
import static com.github.daweizhou89.mbridge.compiler.Consts.CONTEXT;
import static com.github.daweizhou89.mbridge.compiler.Consts.DOUBEL;
import static com.github.daweizhou89.mbridge.compiler.Consts.FLOAT;
import static com.github.daweizhou89.mbridge.compiler.Consts.FRAGMENT;
import static com.github.daweizhou89.mbridge.compiler.Consts.FRAGMENT_V4;
import static com.github.daweizhou89.mbridge.compiler.Consts.INTEGER;
import static com.github.daweizhou89.mbridge.compiler.Consts.INTENT;
import static com.github.daweizhou89.mbridge.compiler.Consts.LONG;
import static com.github.daweizhou89.mbridge.compiler.Consts.PARCELABLE;
import static com.github.daweizhou89.mbridge.compiler.Consts.SERVICE;
import static com.github.daweizhou89.mbridge.compiler.Consts.SHORT;
import static com.github.daweizhou89.mbridge.compiler.Consts.STRING;
import static com.github.daweizhou89.mbridge.compiler.Consts.CHARSEQUENCE;


/**
 * Created by daweizhou89 on 2017/7/21.
 */

public class TypeUtils {

    private Types types;
    private Elements elements;
    private TypeMirror parcelableType;

    public TypeUtils(Types types, Elements elements) {
        this.types = types;
        this.elements = elements;
        parcelableType = elements.getTypeElement(PARCELABLE).asType();
    }

    /**
     * Diagnostics out the true java type
     *
     * @param element Raw type
     * @return Type class of java
     */
    public int typeExchange(Element element) {
        TypeMirror typeMirror = element.asType();

        // Primitive
        if (typeMirror.getKind().isPrimitive()) {
            return element.asType().getKind().ordinal();
        }

        switch (typeMirror.toString()) {
            case BYTE:
                return TypeKind.BYTE.ordinal();
            case SHORT:
                return TypeKind.SHORT.ordinal();
            case INTEGER:
                return TypeKind.INT.ordinal();
            case LONG:
                return TypeKind.LONG.ordinal();
            case FLOAT:
                return TypeKind.FLOAT.ordinal();
            case DOUBEL:
                return TypeKind.DOUBLE.ordinal();
            case BOOLEAN:
                return TypeKind.BOOLEAN.ordinal();
            case STRING:
                return TypeKind.STRING.ordinal();
            case CHARSEQUENCE:
                return TypeKind.CHARSEQUENCE.ordinal();
            default:    // Other side, maybe the PARCELABLE, SERIALIZABLE or OBJECT.
                if (types.isSubtype(typeMirror, parcelableType)) {  // PARCELABLE
                    return TypeKind.PARCELABLE.ordinal();
                } else {    // For others
                    return TypeKind.OBJECT.ordinal();
                }
        }
    }

    public boolean isObjectType(Element element) {
        TypeMirror typeMirror = element.asType();

        // Primitive
        if (typeMirror.getKind().isPrimitive()) {
            return false;
        }

        switch (typeMirror.toString()) {
            case BYTE:
            case SHORT:
            case INTEGER:
            case LONG:
            case FLOAT:
            case DOUBEL:
            case BOOLEAN:
            case STRING:
            case CHARSEQUENCE:
            case INTENT:
            case CONTEXT:
            case ACTIVITY:
            case SERVICE:
            case FRAGMENT:
            case FRAGMENT_V4:
            case BUNDLE:
                return false;
            default:
                return true;
        }
    }

}
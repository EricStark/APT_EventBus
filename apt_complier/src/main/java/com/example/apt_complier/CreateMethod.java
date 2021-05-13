package com.example.apt_complier;

import com.example.apt_annotations.SubscribMethod;
import com.example.apt_annotations.ThreadMode;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;

import java.util.ArrayList;

import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeMirror;

/**
 * Created by bohou on 2021/4/6 Email: bohou@tencent.com
 * <p>
 * http://www.yq1012.com/api/index.html-index-filesindex-1.html
 */
public class CreateMethod {

    private String fullClassName;
    private ThreadMode annotationValue;
    private String methodParamsType;
    private String methodName;
    private String creatMethodName;
    private static final String SUBSCRIBEMETHOD = "SubscribeMethod";

    public CreateMethod(String fullClassName, String methodName,
                        TypeMirror methodParamsType,
                        ThreadMode annotationValue) {
        this.fullClassName = fullClassName;
        this.methodParamsType = methodParamsType.toString();
        this.annotationValue = annotationValue;
        this.methodName = methodName;
    }

    public String getFullClassName() {
        return fullClassName;
    }

    public Object getMethodName() {
        return creatMethodName;
    }

    private String[] getInformation(String info) {
        StringBuilder stringBuilder = new StringBuilder();
        String[] str = new String[2];
        String[] split = info.split("\\.");
        int length = split.length;
        for (int i = 0; i < length - 1; i++) {
            stringBuilder.append(split[i]);
            if (i != length - 2) {
                stringBuilder.append(".");
            }
        }
        str[0] = stringBuilder.toString();
        str[1] = split[split.length - 1];
        return str;
    }

    /**
     * public static List<SubscribMethod> getMainSubscribeMethod() {
     * List<SubscribMethod> list = new ArrayList<>();
     * <p>
     * list.add(new SubscribMethod(MainActivity.class, "onRecieve", Student.class, ThreadMode.MAIN_THREAD));
     * list.add(new SubscribMethod(MainActivity.class, "recieve", Teacher.class, ThreadMode.MAIN_THREAD));
     * return list;
     * }
     * <p>
     * public static List<SubscribMethod> getSecondSubscribeMethod() {
     * List<SubscribMethod> list = new ArrayList<>();
     * <p>
     * list.add(new SubscribMethod(MainActivity.class, "recieve", Teacher.class, ThreadMode.MAIN_THREAD));
     * return list;
     * }
     * $L for Literals
     * $S for Strings
     * $T for Types
     *
     * @return MethodSpec
     */
    public MethodSpec generateMethod() {
        /**
         * Information about the registration class
         */
        String[] classInfos = getInformation(fullClassName);
        String registedClassPackageName = classInfos[0];
        String registedClassSimpleClassName = classInfos[1];
        /**
         * Information about registration method parameters
         */
        String[] methodParamsTypeInfos = getInformation(methodParamsType);
        String registedMethodParamsTypePackageName = methodParamsTypeInfos[0];
        String registedMethodParamsTypeName = methodParamsTypeInfos[1];

        creatMethodName = registedClassSimpleClassName + methodName + SUBSCRIBEMETHOD;

        /**
         * Build MethodSpec
         */
        ClassName arrayList = ClassName.get(ArrayList.class);
        ClassName subMethod = ClassName.get(SubscribMethod.class);
        ClassName registedClazz = ClassName.get(registedClassPackageName, registedClassSimpleClassName);
        ClassName param = ClassName.get(registedMethodParamsTypePackageName, registedMethodParamsTypeName);
        TypeName typeName = ParameterizedTypeName.get(arrayList, subMethod);
        MethodSpec methodSpec = MethodSpec.methodBuilder(creatMethodName)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addStatement("$T list = new $T<>()", typeName, arrayList)
                .addStatement("$T subm = new $T($T.class,$S,$T.class,$T." + annotationValue + ")"
                        , subMethod, subMethod, registedClazz, methodName, param, annotationValue.getClass())
                .addStatement("list.add(subm)")
                .addStatement("return list")
                .returns(typeName)
                .build();
        return methodSpec;
    }
}

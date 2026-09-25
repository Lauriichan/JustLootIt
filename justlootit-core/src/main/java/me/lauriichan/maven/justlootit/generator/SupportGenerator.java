package me.lauriichan.maven.justlootit.generator;

import java.lang.annotation.Annotation;

import org.jboss.forge.roaster.model.source.AnnotationSource;
import org.jboss.forge.roaster.model.source.AnnotationTargetSource;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.MethodSource;

import me.lauriichan.maven.sourcemod.api.ISourceGenerator;
import me.lauriichan.maven.sourcemod.api.source.SourcePackage;
import me.lauriichan.spigot.justlootit.compatibility.support.SupportContinue;
import me.lauriichan.spigot.justlootit.compatibility.support.SupportHelper;

public class SupportGenerator implements ISourceGenerator {

    private static record Reference(Class<?> type, String name) {}

    private static final Reference SUPPORT_CONTINUE = new Reference(SupportHelper.class, "abortOnChange");
    private static final Reference SUPPORT_DEFAULT = new Reference(SupportHelper.class, "DEFAULT_TRUE");

    @Override
    public void generateSources(SourcePackage root) {
        SourcePackage supportPkg = root.findPackage("me.lauriichan.spigot.justlootit.compatibility.support");
        if (supportPkg == null) {
            return;
        }
        JavaClassSource supportBase = supportPkg.findClass("AbstractSupport").orElse(null);
        if (supportBase == null) {
            return;
        }
        supportPkg.interfaceStream().forEach(supportInterface -> {
            String className = supportInterface.getName().substring(1);
            JavaClassSource supportImpl = supportPkg.createClass(className);
            supportImpl.setSuperType(supportBase.getQualifiedName() + '<' + supportInterface.getName() + '>');
            supportImpl.addInterface(supportInterface);
            supportImpl.removeImport(supportBase);
            supportImpl.removeImport(supportInterface);
            supportImpl.setFinal(true);
            supportImpl.addField("public static final %1$s INSTANCE = new %1$s();".formatted(className));
            supportImpl.addMethod().setPrivate().setConstructor(true).setBody("""
                if (INSTANCE != null) {
                    throw new UnsupportedOperationException();
                }
                """);
            for (var method : supportInterface.getMethods()) {
                Reference continueRef = getReference(method, SupportContinue.class, SUPPORT_CONTINUE);
                Reference defaultRef = getReference(method, SupportContinue.class, SUPPORT_DEFAULT);
                MethodSource<JavaClassSource> methodSrc = supportImpl.addMethod();
                methodSrc.setName(method.getName());
                methodSrc.setPublic().setFinal(true);
                methodSrc.setReturnType(method.getReturnType());
                StringBuilder callBuilder = new StringBuilder(".");
                callBuilder.append(method.getName()).append('(');
                for (var param : method.getParameters()) {
                    callBuilder.append(param.getName()).append(',');
                    methodSrc.addParameter(param.getType().getQualifiedNameWithGenerics(), param.getName());
                }
                callBuilder.deleteCharAt(callBuilder.length() - 1);
                String methodCall = callBuilder.append(')').toString();
                StringBuilder bodyBuilder = new StringBuilder();
                bodyBuilder.append(method.getReturnType().getQualifiedName()).append(" output = ").append(defaultRef.type().getName())
                    .append('.').append(defaultRef.name()).append(", tmp;");
                bodyBuilder.append("for (var impl : implementations) {");
                bodyBuilder.append("tmp = impl").append(methodCall).append(";");
                bodyBuilder.append("if (!").append(continueRef.type().getName()).append('.').append(continueRef.name())
                    .append("(output, tmp)) {");
                bodyBuilder.append("return tmp; }");
                bodyBuilder.append("output = tmp; }");
                bodyBuilder.append("return output;");
                methodSrc.setBody(bodyBuilder.toString());
            }
        });
    }

    private Reference getReference(AnnotationTargetSource<?, ?> src, Class<? extends Annotation> annotationType, Reference defaultRef) {
        AnnotationSource<?> annoSrc = src.getAnnotation(annotationType);
        if (annoSrc == null) {
            return defaultRef;
        }
        return new Reference(getClass(annoSrc, "type", defaultRef.type()), getString(annoSrc, "name", defaultRef.name()));
    }

    private Class<?> getClass(AnnotationSource<?> annotation, String name, Class<?> defaultValue) {
        Class<?> clazz = annotation.getClassValue(name);
        if (clazz == null) {
            return defaultValue;
        }
        return clazz;
    }

    private String getString(AnnotationSource<?> annotation, String name, String defaultValue) {
        String string = annotation.getStringValue(name);
        if (string == null || string.isBlank()) {
            return defaultValue;
        }
        return string;
    }

}

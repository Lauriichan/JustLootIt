package me.lauriichan.maven.justlootit.generator;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.jboss.forge.roaster.model.Visibility;
import org.jboss.forge.roaster.model.source.AnnotationSource;
import org.jboss.forge.roaster.model.source.AnnotationTargetSource;
import org.jboss.forge.roaster.model.source.FieldSource;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaDocCapableSource;
import org.jboss.forge.roaster.model.source.JavaSource;
import org.jboss.forge.roaster.model.source.MemberSource;
import org.jboss.forge.roaster.model.source.MethodSource;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import me.lauriichan.maven.justlootit.annotation.Event;
import me.lauriichan.maven.justlootit.annotation.EventField;
import me.lauriichan.maven.sourcemod.api.ISourceGenerator;
import me.lauriichan.maven.sourcemod.api.SourceTransformerUtils;
import me.lauriichan.maven.sourcemod.api.source.SourcePackage;

public class JLIEventGenerator implements ISourceGenerator {

    private static record ConstructorParam(String type, String name, int sort, boolean toSuper, boolean allowNull) {}

    @Override
    public void generateSources(SourcePackage root) {
        SourcePackage eventPkg = root.findPackage("me.lauriichan.spigot.justlootit.api.event");
        if (eventPkg == null) {
            return;
        }
        ObjectArrayList<SourcePackage> queue = new ObjectArrayList<>();
        queue.push(eventPkg);
        while (!queue.isEmpty()) {
            SourcePackage pkg = queue.pop();
            pkg.getPackages().forEach(queue::push);
            pkg.classStream().forEach(source -> transformClass(root, pkg, source));
        }
    }

    private void transformClass(SourcePackage root, SourcePackage pkg, JavaClassSource source) {
        if (!source.getName().endsWith("Tmp")) {
            return;
        }
        String superTypeValue = source.getSuperType();
        if (superTypeValue == null || superTypeValue.isBlank() || superTypeValue.startsWith("java.lang")) {
            return;
        }
        AnnotationSource<JavaClassSource> eventAnnotation = source.getAnnotation(Event.class);
        if (eventAnnotation == null) {
            return;
        }

        JavaClassSource newSource = pkg.createClass(source.getName().substring(0, source.getName().length() - 3));
        newSource.setSuperType(superTypeValue.endsWith("Tmp") ? superTypeValue.substring(0, superTypeValue.length() - 3) : superTypeValue);
        newSource.setVisibility(Visibility.PUBLIC);
        newSource.setAbstract(getBoolean(eventAnnotation, "isAbstract", Event.DEFAULT_IS_ABSTRACT));

        SourceTransformerUtils.importClass(newSource, "org.bukkit.event.HandlerList");
        newSource.addField("private static final HandlerList handlers = new HandlerList();");

        ObjectArrayList<ConstructorParam> params = new ObjectArrayList<>();
        AtomicBoolean canBeCancellable = new AtomicBoolean(true);
        findConstructorParams(root, source, params, canBeCancellable);
        params.sort((p1, p2) -> Integer.compare(p2.sort(), p1.sort()));

        boolean genCancellable;
        if (genCancellable = canBeCancellable.get() && getBoolean(eventAnnotation, "cancellable", Event.DEFAULT_CANCELLABLE)) {
            SourceTransformerUtils.importClass(newSource, "org.bukkit.event.Cancellable");
            newSource.addInterface("org.bukkit.event.Cancellable");
            newSource.addField("private boolean cancelled = false;");
        }

        MethodSource<JavaClassSource> constructor = newSource.addMethod().setConstructor(true);
        StringBuilder superBuilder = new StringBuilder().append("super(");
        boolean firstSuper = true;
        StringBuilder paramBuilder = new StringBuilder();
        for (ConstructorParam param : params) {
            if (param.toSuper()) {
                if (!firstSuper) {
                    superBuilder.append(", ");
                } else {
                    firstSuper = false;
                }
                superBuilder.append(param.name());
            } else {
                paramBuilder.append("\nthis.").append(param.name()).append(" = ");
                if(param.allowNull()) {
                    paramBuilder.append(param.name());
                } else {
                    paramBuilder.append("Objects.requireNonNull(").append(param.name()).append(", \"").append(param.name()).append("\")");
                    SourceTransformerUtils.importClass(newSource, Objects.class);
                }
                paramBuilder.append(";");
            }
            constructor.addParameter(param.type(), param.name());
        }
        params.clear();
        constructor.setPublic().setBody(superBuilder.append(");").append(paramBuilder).toString());

        source.getFields().forEach(field -> {
            FieldSource<JavaClassSource> newField = newSource.addField();
            newField.setType(field.getType().getQualifiedNameWithGenerics());
            if (!(field.getLiteralInitializer() == null || field.getLiteralInitializer().isBlank())) {
                newField.setLiteralInitializer(field.getLiteralInitializer());
            }
            newField.setVolatile(field.isVolatile());
            newField.setTransient(field.isTransient());
            copy(newField, field);
            AnnotationSource<JavaClassSource> annotation = newField.getAnnotation(EventField.class);
            if (annotation == null) {
                return;
            }
            newField.removeAnnotation(annotation);
            if (getBoolean(annotation, "getter", EventField.DEFAULT_GETTER)) {
                newSource.addMethod().setPublic().setFinal(true).setName(newField.getName()).setReturnType(newField.getType()).setBody("""
                    return %1$s;
                    """.formatted(newField.getName()));
            }
            if (getBoolean(annotation, "setter", EventField.DEFAULT_SETTER)) {
                boolean allowNull = field.getType().isPrimitive() || getBoolean(annotation, "allowNull", EventField.DEFAULT_ALLOW_NULL);
                newSource.addMethod().setPublic().setFinal(true).setName(newField.getName())
                    .setBody((allowNull ? """
                        this.%1$s = %1$s;
                        """ : """
                        this.%1$s = Objects.requireNonNull(%1$s, "%1$s");
                        """).formatted(newField.getName()))
                    .addParameter(newField.getType().getQualifiedNameWithGenerics(), newField.getName()).setFinal(true);
                if (!allowNull) {
                    SourceTransformerUtils.importClass(newSource, Objects.class);
                }
            } else {
                newField.setFinal(true);
            }
        });
        source.getMethods().forEach(method -> {
            if (method.isConstructor()) {
                return;
            }
            MethodSource<JavaClassSource> newMethod = newSource.addMethod();
            copy(newMethod, method);
            newMethod.setBody(method.getBody());
            method.getParameters().forEach(param -> {
                copyAnnotation(newMethod.addParameter(param.getType().getQualifiedNameWithGenerics(), param.getName())
                    .setFinal(param.isFinal()).setVarArgs(param.isVarArgs()), param);
            });
            method.getTypeVariables().forEach(typeVar -> {
                newMethod.addTypeVariable(typeVar.getName()).setBounds(
                    typeVar.getBounds().stream().map(type -> type.getQualifiedNameWithGenerics()).toArray(String[]::new));
            });
            newMethod.setReturnType(method.getReturnType().getQualifiedNameWithGenerics());
        });
        source.getTypeVariables().forEach(typeVar -> {
            newSource.addTypeVariable(typeVar.getName())
                .setBounds(typeVar.getBounds().stream().map(type -> source.resolveType(type.getQualifiedName())).toArray(String[]::new));
        });

        if (genCancellable) {
            newSource.addMethod("@Override public final boolean isCancelled() { return cancelled; }");
            newSource.addMethod("@Override public final void setCancelled(final boolean cancelled) { this.cancelled = cancelled; }");
        }

        newSource.addMethod("@Override public HandlerList getHandlers() { return handlers; }");
        newSource.addMethod("public static HandlerList getHandlerList() { return handlers; }");

        copyJavaDoc(newSource, source);
        source.getNestedTypes().forEach(newSource::addNestedType);
        source.getImports().forEach(imp -> {
            if (newSource.hasImport(imp)) {
                return;
            }
            newSource.addImport(imp);
        });
        source.getInterfaces().forEach(intf -> {
            if (newSource.hasInterface(intf)) {
                return;
            }
            newSource.addInterface(intf);
        });

        for (AnnotationSource<JavaClassSource> annotation : source.getAnnotations()) {
            if (annotation.getQualifiedName().equals(Event.class.getName())) {
                continue;
            }
            AnnotationSource<JavaClassSource> newAnnotation = newSource.addAnnotation(newSource.resolveType(annotation.getQualifiedName()));
            annotation.getValues().forEach(pair -> newAnnotation.setLiteralValue(pair.getName(), pair.getLiteralValue()));
        }
        newSource.removeImport(Event.class);
        newSource.removeImport(EventField.class);

        if (superTypeValue.startsWith(pkg.classpath())) {
            String[] parts = superTypeValue.split("\\.");
            if (superTypeValue.substring(0, superTypeValue.length() - 1 - parts[parts.length - 1].length()).equals(pkg.classpath())) {
                newSource.addAnnotation(SuppressWarnings.class).setStringValue("unused");
            }
        }
    }

    private void copy(MemberSource<JavaClassSource, ?> newMember, MemberSource<JavaClassSource, ?> member) {
        newMember.setName(member.getName());
        newMember.setVisibility(member.getVisibility());
        newMember.setFinal(member.isFinal());
        newMember.setStatic(member.isStatic());
        copyJavaDoc(newMember, member);
        copyAnnotation(newMember, member);
    }

    private void copyJavaDoc(JavaDocCapableSource<?> newDoc, JavaDocCapableSource<?> doc) {
        if (!doc.getJavaDoc().getFullText().isBlank()) {
            newDoc.getJavaDoc().setFullText(doc.getJavaDoc().getFullText());
        }
    }

    private void copyAnnotation(AnnotationTargetSource<JavaClassSource, ?> newTarget, AnnotationTargetSource<JavaClassSource, ?> target) {
        for (AnnotationSource<JavaClassSource> annotation : target.getAnnotations()) {
            AnnotationSource<JavaClassSource> newAnnotation = newTarget
                .addAnnotation(target.getOrigin().resolveType(annotation.getQualifiedName()));
            annotation.getValues().forEach(pair -> newAnnotation.setLiteralValue(pair.getName(), pair.getLiteralValue()));
        }
    }

    private void findConstructorParams(SourcePackage root, JavaClassSource startingSource, ObjectArrayList<ConstructorParam> params,
        AtomicBoolean canBeCancellable) {
        ObjectArrayList<JavaClassSource> queue = new ObjectArrayList<>();
        queue.push(startingSource);
        AtomicInteger sort = new AtomicInteger(0);
        while (!queue.isEmpty()) {
            JavaClassSource source = queue.pop();
            source.getFields().forEach(field -> {
                if (!field.hasAnnotation(EventField.class)) {
                    return;
                }
                AnnotationSource<JavaClassSource> eventFieldAnnotation = field.getAnnotation(EventField.class);
                if (!getBoolean(eventFieldAnnotation, "constructor", EventField.DEFAULT_CONSTRUCTOR)) {
                    return;
                }
                params.add(new ConstructorParam(field.getType().getQualifiedNameWithGenerics(), field.getName(), sort.get(),
                    source != startingSource, field.getType().isPrimitive() || getBoolean(eventFieldAnnotation, "allowNull", EventField.DEFAULT_ALLOW_NULL)));
            });
            AnnotationSource<JavaClassSource> eventAnnotation = source.getAnnotation(Event.class);
            if (eventAnnotation == null) {
                continue;
            }
            if (canBeCancellable.get() && source != startingSource) {
                canBeCancellable.set(!getBoolean(eventAnnotation, "cancellable", Event.DEFAULT_CANCELLABLE));
            }
            String resolvedTypeValue = source.resolveType(source.getSuperType());
            JavaSource<?> otherSource = root.findSource(resolvedTypeValue).orElse(null);
            if (otherSource instanceof JavaClassSource classSource) {
                sort.addAndGet(1);
                queue.push(classSource);
            }
        }
    }

    private boolean getBoolean(AnnotationSource<JavaClassSource> annotation, String name, boolean defaultValue) {
        String literal = annotation.getLiteralValue(name);
        if (literal == null || literal.isBlank()) {
            return defaultValue;
        }
        return "true".equals(literal);
    }

}

package me.lauriichan.maven.justlootit.generator;

import java.util.ArrayList;
import java.util.List;

import org.jboss.forge.roaster.model.source.EnumConstantSource;
import org.jboss.forge.roaster.model.source.FieldSource;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaEnumSource;
import org.jboss.forge.roaster.model.Type;

import me.lauriichan.maven.sourcemod.api.ISourceGenerator;
import me.lauriichan.maven.sourcemod.api.source.SourcePackage;

public final class ContainerTypeGenerator implements ISourceGenerator {

    @Override
    public void generateSources(SourcePackage root) {
        SourcePackage pkg = root.getOrCreatePackage("me.lauriichan.spigot.justlootit.data");
        JavaEnumSource source = pkg.createEnum("ContainerType");
        JavaClassSource container = pkg.findClass("Container").orElse(null);
        if (container == null) {
            return;
        }
        JavaClassSource methodProvider = pkg.parent().getOrCreatePackage("command.helper").findClass("ContainerCreationHelper")
            .orElse(null);
        source.addField("private final Class<? extends Container> containerType;");
        source.addMethod("public Class<? extends Container> containerType() { return containerType; }");
        source.addMethod("@Override public String toString() { return name().toLowerCase(); }");
        boolean creatable;
        JavaClassSource blockCreatorClass, entityCreatorClass;
        if (methodProvider != null) {
            blockCreatorClass = (JavaClassSource) methodProvider.getNestedType("BlockContainerCreator");
            entityCreatorClass = (JavaClassSource) methodProvider.getNestedType("EntityContainerCreator");
            if (blockCreatorClass != null && entityCreatorClass != null) {
                source.addImport(methodProvider);
                source.addImport(methodProvider.getQualifiedName() + '.' + blockCreatorClass.getName());
                source.addImport(methodProvider.getQualifiedName() + '.' + entityCreatorClass.getName());
                source.addField().setName("blockCreator").setType(blockCreatorClass.getName()).setFinal(true).setPrivate();
                source.addField().setName("entityCreator").setType(entityCreatorClass.getName()).setFinal(true).setPrivate();
                source.addMethod("public BlockContainerCreator blockCreator() { return blockCreator; }");
                source.addMethod("public EntityContainerCreator entityCreator() { return entityCreator; }");
                source.addMethod(
                    "private ContainerType(Class<? extends Container> containerType, EntityContainerCreator entityCreator, BlockContainerCreator blockCreator) { this.containerType = containerType; this.entityCreator = entityCreator; this.blockCreator = blockCreator; }")
                    .setConstructor(true);
                
                creatable = true;
            } else {
                creatable = false;
            }
        } else {
            creatable = false;
            blockCreatorClass = entityCreatorClass = null;
        }
        if (!creatable) {
            source.addMethod("private ContainerType(Class<? extends Container> containerType) { this.containerType = containerType; }")
                .setConstructor(true);
        }

        pkg.classStream().filter(src -> isContainerSubtype(src, container.getName(), container.getQualifiedName())).forEach(clazz -> {
            String name = clazz.getName();
            int idx = name.indexOf("Container");
            if (idx != -1) {
                name = name.substring(0, idx);
            }
            StringBuilder builder = new StringBuilder();
            StringBuilder word = new StringBuilder();
            char[] chars = name.toCharArray();
            for (char ch : chars) {
                if (!Character.isAlphabetic(ch) && !Character.isDigit(ch)) {
                    continue;
                }
                if (Character.isUpperCase(ch)) {
                    if (word.isEmpty()) {
                        word.append(ch);
                        continue;
                    }
                    if (!builder.isEmpty()) {
                        builder.append('_');
                    }
                    builder.append(word);
                    word = new StringBuilder();
                    word.append(ch);
                    continue;
                }
                word.append(Character.toUpperCase(ch));
            }
            if (!word.isEmpty()) {
                if (!builder.isEmpty()) {
                    builder.append('_');
                }
                builder.append(word);
            }
            EnumConstantSource enumSource = source.addEnumConstant().setName(builder.toString());
            if (methodProvider != null && creatable) {
                ArrayList<String> args = new ArrayList<>();
                args.add(clazz.getQualifiedName() + ".class");
                List<FieldSource<JavaClassSource>> fields = methodProvider.getFields().stream().filter(field -> isValidField(field, clazz))
                    .toList();
                fields.stream().filter(field -> isType(field, entityCreatorClass)).findAny()
                    .ifPresentOrElse((field) -> args.add(methodProvider.getName() + '.' + field.getName()), () -> args.add("null"));
                fields.stream().filter(field -> isType(field, blockCreatorClass)).findAny()
                    .ifPresentOrElse((field) -> args.add(methodProvider.getName() + '.' + field.getName()), () -> args.add("null"));
                enumSource.setConstructorArguments(args.toArray(String[]::new));
            } else {
                enumSource.setConstructorArguments(clazz.getQualifiedName() + ".class");
            }
        });
    }

    private boolean isType(FieldSource<JavaClassSource> fieldSource, JavaClassSource source) {
        Type<JavaClassSource> type = fieldSource.getType();
        String qualifiedName = source.getQualifiedName();
        if (qualifiedName.contains("$")) {
            JavaClassSource origin = fieldSource.getOrigin();
            if (source.getOrigin().equals(origin)) {
                return type.isType(qualifiedName.replace(origin.getName() + '$', ""));
            }
            return type.isType(qualifiedName.replace('$', '.'));
        }
        return type.isType(qualifiedName);
    }

    private boolean isValidField(FieldSource<JavaClassSource> fieldSource, JavaClassSource source) {
        if (!fieldSource.isStatic() || !fieldSource.isFinal()) {
            return false;
        }
        String literal = fieldSource.getLiteralInitializer();
        int index = literal.indexOf('(');
        if (index == -1) {
            return false;
        }
        literal = literal.substring(index + 1);
        index = literal.indexOf(',');
        if (index == -1) {
            return false;
        }
        literal = literal.substring(0, index);
        if (literal.endsWith(".class")) {
            literal = literal.substring(0, literal.length() - 6);
        }
        return source.getName().equals(literal) || source.getQualifiedName().equals(literal);
    }

    private boolean isContainerSubtype(JavaClassSource source, String containerName, String containerQualifiedName) {
        if (source.isAbstract()) {
            return false;
        }
        if (source.hasImport(containerQualifiedName)) {
            return source.getSuperType().equals(containerName);
        }
        return source.getSuperType().equals(containerQualifiedName);
    }

}

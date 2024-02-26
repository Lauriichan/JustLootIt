package me.lauriichan.maven.justlootit.generator;

import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaEnumSource;

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
            source.addEnumConstant().setName(builder.toString());
        });
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

package me.lauriichan.spigot.justlootit.nms.version;

import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import javax.tools.Diagnostic.Kind;

import me.lauriichan.laylib.json.IJson;
import me.lauriichan.laylib.json.JsonArray;
import me.lauriichan.laylib.json.JsonObject;
import me.lauriichan.laylib.json.io.JsonWriter;
import me.lauriichan.laylib.logger.util.StringUtil;
import me.lauriichan.spigot.justlootit.platform.PlatformType;

public final class VersionImplProcessor extends AbstractProcessor {

    public static final String PLATFORM_VERSION_RESOURCE = "META-INF/platform_version.json";

    private Types typeHelper;
    private Elements elementHelper;

    private TypeMirror versionHandlerType;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);

        this.typeHelper = processingEnv.getTypeUtils();
        this.elementHelper = processingEnv.getElementUtils();
        this.versionHandlerType = elementHelper.getTypeElement("me.lauriichan.spigot.justlootit.nms.VersionHandler").asType();
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton(VersionImpl.class.getName());
    }

    @Override
    public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
        if (annotations.isEmpty()) {
            return false;
        }
        JsonObject root = new JsonObject();
        for (final Element element : roundEnv.getElementsAnnotatedWith(VersionImpl.class)) {
            if (element.getKind() != ElementKind.CLASS) {
                continue;
            }
            VersionImpl versionImpl = element.getAnnotation(VersionImpl.class);
            if (versionImpl == null || !typeHelper.isAssignable(element.asType(), versionHandlerType)) {
                continue;
            }
            JsonObject implObj = new JsonObject();
            root.put(versionImpl.name(), implObj);
            JsonArray platforms = new JsonArray();
            for (PlatformType type : versionImpl.platforms()) {
                platforms.add(IJson.of(type.name()));
            }
            JsonArray supportedVersions = new JsonArray();
            for (String version : versionImpl.versions()) {
                supportedVersions.add(IJson.of(version));
            }
            implObj.put("class", element.toString());
            implObj.put("platforms", platforms);
            implObj.put("versions", supportedVersions);
        }
        if (root.isEmpty()) {
            return false;
        }
        JsonWriter jsonWriter = new JsonWriter();
        jsonWriter.setPretty(false);
        try {
            processingEnv.getFiler().getResource(StandardLocation.CLASS_OUTPUT, "", PLATFORM_VERSION_RESOURCE);
            FileObject file = processingEnv.getFiler().createResource(StandardLocation.CLASS_OUTPUT, "", PLATFORM_VERSION_RESOURCE);
            try (Writer writer = file.openWriter()) {
                jsonWriter.toWriter(root, writer);
            }
        } catch (final IOException e) {
            log(Kind.ERROR, StringUtil.stackTraceToString(e));
        }
        return true;
    }

    /*
     * Logging
     */

    private void log(final Kind kind, final String message, final Object... arguments) {
        final String out = String.format(message, arguments);
        processingEnv.getMessager().printMessage(kind, out);
        if (kind == Kind.ERROR) {
            System.out.println("[ERROR] " + out);
            return;
        }
        if (kind == Kind.WARNING) {
            System.out.println("[WARNING] " + out);
            return;
        }
        System.out.println("[INFO] " + out);
    }

}

package poussecafe.doc;

import com.sun.javadoc.LanguageVersion;
import com.sun.javadoc.RootDoc;
import java.io.File;
import java.util.Objects;
import poussecafe.runtime.Runtime;

public class PousseCafeDoclet {

    public PousseCafeDoclet(RootDoc rootDoc) {
        Objects.requireNonNull(rootDoc);
        rootDocWrapper = new RootDocWrapper(rootDoc);
        Logger.setRootDoc(rootDocWrapper);

        runtime = new Runtime.Builder()
                .withBoundedContext(PousseCafeDoc.configure().defineAndImplementDefault().build())
                .build();
    }

    private RootDocWrapper rootDocWrapper;

    private Runtime runtime;

    public void start() {
        Logger.info("Starting Pousse-Café doclet...");
        runtime.start();

        analyzeCode();
        createOutputFolder();

        writeGraphs();
        writeHtml();
        writePdf();
    }

    private void analyzeCode() {
        detectBoundedContexts();
        detectBoundedContextComponents();
        detectDomainProcesses();
        detectRelations();
    }

    private void detectBoundedContexts() {
        BoundedContextDocCreator boundedContextCreator = new BoundedContextDocCreator();
        runtime.injector().injectDependenciesInto(boundedContextCreator);

        PackagesAnalyzer codeAnalyzer = new PackagesAnalyzer.Builder()
                .rootDocWrapper(rootDocWrapper)
                .packageDocConsumer(boundedContextCreator)
                .build();
        codeAnalyzer.analyzeCode();
    }

    private void detectBoundedContextComponents() {
        AggregateDocCreator aggregateDocCreator = new AggregateDocCreator(rootDocWrapper);
        runtime.injector().injectDependenciesInto(aggregateDocCreator);

        ServiceDocCreator serviceDocCreator = new ServiceDocCreator(rootDocWrapper);
        runtime.injector().injectDependenciesInto(serviceDocCreator);

        EntityDocCreator entityDocCreator = new EntityDocCreator(rootDocWrapper);
        runtime.injector().injectDependenciesInto(entityDocCreator);

        ValueObjectDocCreator valueObjectDocCreator = new ValueObjectDocCreator(rootDocWrapper);
        runtime.injector().injectDependenciesInto(valueObjectDocCreator);

        FactoryDocCreator factoryDocCreator = new FactoryDocCreator(rootDocWrapper);
        runtime.injector().injectDependenciesInto(factoryDocCreator);

        MessageListenerDocCreator messageListenerDocCreator = new MessageListenerDocCreator(rootDocWrapper);
        runtime.injector().injectDependenciesInto(messageListenerDocCreator);

        ClassesAnalyzer codeAnalyzer = new ClassesAnalyzer.Builder()
                .rootDocWrapper(rootDocWrapper)
                .classDocConsumer(aggregateDocCreator)
                .classDocConsumer(serviceDocCreator)
                .classDocConsumer(entityDocCreator)
                .classDocConsumer(valueObjectDocCreator)
                .classDocConsumer(factoryDocCreator)
                .classDocConsumer(messageListenerDocCreator)
                .build();
        codeAnalyzer.analyzeCode();
    }

    private void detectDomainProcesses() {
        DomainProcessDocCreator domainProcessDocCreator = new DomainProcessDocCreator(rootDocWrapper);
        runtime.injector().injectDependenciesInto(domainProcessDocCreator);

        ClassesAnalyzer codeAnalyzer = new ClassesAnalyzer.Builder()
                .rootDocWrapper(rootDocWrapper)
                .classDocConsumer(domainProcessDocCreator)
                .build();
        codeAnalyzer.analyzeCode();
    }

    private void detectRelations() {
        RelationCreator relationCreator = new RelationCreator(rootDocWrapper);
        runtime.injector().injectDependenciesInto(relationCreator);

        ClassesAnalyzer codeAnalyzer = new ClassesAnalyzer.Builder()
                .rootDocWrapper(rootDocWrapper)
                .classDocConsumer(relationCreator)
                .build();
        codeAnalyzer.analyzeCode();
    }

    private void createOutputFolder() {
        File outputDirectory = new File(rootDocWrapper.outputPath());
        outputDirectory.mkdirs();
    }

    private void writeGraphs() {
        GraphImagesWriter graphsWriter = new GraphImagesWriter(rootDocWrapper);
        runtime.injector().injectDependenciesInto(graphsWriter);
        graphsWriter.writeImages();
    }

    private void writeHtml() {
        HtmlWriter htmlWriter = new HtmlWriter(rootDocWrapper);
        runtime.injector().injectDependenciesInto(htmlWriter);
        htmlWriter.writeHtml();
    }

    private void writePdf() {
        PdfWriter pdfWriter = new PdfWriter(rootDocWrapper);
        runtime.injector().injectDependenciesInto(pdfWriter);
        pdfWriter.writePdf();
    }

    public static boolean start(RootDoc root) {
        new PousseCafeDoclet(root).start();
        return true;
    }

    public static int optionLength(String option) {
        if ("-output".equals(option)) {
            return 2;
        }
        if ("-debug".equals(option)) {
            return 1;
        }
        if ("-version".equals(option)) {
            return 2;
        }
        if ("-domain".equals(option)) {
            return 2;
        }
        if ("-basePackage".equals(option)) {
            return 2;
        }
        return 0;
    }

    public static LanguageVersion languageVersion() {
        return LanguageVersion.JAVA_1_5;
     }
}

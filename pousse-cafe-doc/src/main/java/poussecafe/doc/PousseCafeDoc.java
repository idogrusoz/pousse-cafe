package poussecafe.doc;

import poussecafe.discovery.BundleConfigurer;

public class PousseCafeDoc {

    private PousseCafeDoc() {

    }

    public static BundleConfigurer configure() {
        return new BundleConfigurer.Builder()
                .moduleBasePackage("poussecafe.doc")
                .build();
    }
}

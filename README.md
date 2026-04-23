# Java Sample to Convert to PDF

A minimal Java sample that converts a document to PDF by calling Nutrient
Document Converter Services over SOAP, using a **Jakarta EE 9+ compatible**
(`jakarta.*` namespace) web-service client.

The project regenerates its SOAP stubs from the Document Converter Services
WSDL at build time with `jaxws-maven-plugin` 4.x, so no pre-built client JAR
is required.

Repository: [Muhimbi/ConvertToPDF-Java](https://github.com/Muhimbi/ConvertToPDF-Java)

## Requirements

- JDK 17 or later
- Apache Maven 3.8+
- Network access to a running Nutrient Document Converter Services instance
  (version 12.x tested). The WSDL URL is typically:
  `http://<dcs-host>:41734/Muhimbi.DocumentConverter.WebService/?wsdl`

## Project layout

```
JConvertPDF/
├── pom.xml                                          Maven build + wsimport config
└── src/main/java/com/muhimbi/sample/WsClient.java   Sample client
```

Generated SOAP stubs land in `target/generated-sources/wsimport/com/muhimbi/ws/`
after the first build — they are *not* checked in.

## Configuration

Two values need to point at your Document Converter Services install:

1. **`<dcs.wsdl.url>` in `pom.xml`** — used by `wsimport` to generate stubs.
2. **`WSDL_URL` in `WsClient.java`** — used at runtime to locate the service.

Keep both in sync.

The input file to convert is controlled by `INPUT_FILE_PATH` in `WsClient.java`.
A command-line argument, if supplied, overrides the constant.

## Build

```
git clone https://github.com/Muhimbi/ConvertToPDF-Java.git
cd ConvertToPDF-Java
mvn clean compile
```

This will:

1. Fetch the WSDL from `<dcs.wsdl.url>`.
2. Generate Jakarta-namespace SOAP stubs (`jakarta.xml.ws.*`,
   `jakarta.xml.bind.*`, `jakarta.jws.*`) into
   `target/generated-sources/wsimport/com/muhimbi/ws/`.
3. Compile both the generated stubs and `WsClient.java`.

## Run

From the project root after a successful build:

```
mvn exec:java -Dexec.mainClass=com.muhimbi.sample.WsClient
```

or, with a specific input path:

```
mvn exec:java -Dexec.mainClass=com.muhimbi.sample.WsClient \
              -Dexec.args="/absolute/path/to/input.docx"
```

The converted PDF is written next to the source file
(e.g. `/path/input.docx` → `/path/input.pdf`).

You can also run `WsClient` directly from your IDE once `mvn compile` has
generated the stubs.

## Troubleshooting

### `Could not find wsdl:service in the provided WSDL(s)`

The `?wsdl` response from Document Converter Services embeds its configured
`baseAddress` hostname in internal WSDL imports. If your build machine cannot
resolve that hostname, `wsimport` fails with this error even if the root WSDL
was fetched successfully.

Fix options (see Nutrient KB:
[Generating web service proxies against a remote machine fails](https://www.nutrient.io/guides/document-converter/power-automate/knowledge-base/generating-web-service-proxies-against-a-remote-machine-fails/)):

- **Client side** — add a hosts-file entry mapping the FQDN in the WSDL
  imports to a reachable IP. On macOS/Linux, edit `/etc/hosts`; on Windows,
  `C:\Windows\System32\drivers\etc\hosts`.
- **Server side** — on the DCS host, open
  `Muhimbi.DocumentConverter.Service.exe.config`, change `baseAddress` to a
  hostname or IP every client can resolve, and restart the
  `Muhimbi Document Converter Service` Windows service.

### `package com.muhimbi.ws does not exist` in the IDE

The generated stubs only appear after the first successful `mvn compile`. Run
the Maven build from a terminal, then reload the Maven project in your IDE so
it picks up `target/generated-sources/wsimport` as a source root.

## Why the Jakarta migration matters

The legacy WSDL-generated client for Document Converter Services used
`javax.xml.ws.*` / `javax.xml.bind.*` / `javax.jws.*`, which were renamed to
`jakarta.*` in Jakarta EE 9. Code targeting Jakarta EE 9+ will not compile
against the old `javax`-namespace stubs. Regenerating with
`jaxws-maven-plugin` 4.x (as this project does) produces stubs that import
the new `jakarta.*` packages; the Document Converter server itself and its
SOAP contract are unchanged, so no server-side update is required.

## References

- [Nutrient Document Converter Services — Java integration](https://www.nutrient.io/blog/convert-office-files-to-pdf-using-java/)
- [Nutrient KB — generating web service proxies against a remote machine](https://www.nutrient.io/guides/document-converter/power-automate/knowledge-base/generating-web-service-proxies-against-a-remote-machine-fails/)
- [Eclipse Metro / JAX-WS RI](https://eclipse-ee4j.github.io/metro-jax-ws/)

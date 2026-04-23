package com.muhimbi.sample;

import com.muhimbi.ws.ConversionQuality;
import com.muhimbi.ws.ConversionRange;
import com.muhimbi.ws.ConversionSettings;
import com.muhimbi.ws.DocumentConverterService;
import com.muhimbi.ws.DocumentConverterService_Service;
import com.muhimbi.ws.ObjectFactory;
import com.muhimbi.ws.OpenOptions;
import com.muhimbi.ws.OutputFormat;

import javax.xml.namespace.QName;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class WsClient {

    private static final String WSDL_URL =
            "http://dcs-host-server:41734/Muhimbi.DocumentConverter.WebService/?wsdl";

    private static final QName SERVICE_QNAME =
            new QName("http://tempuri.org/", "DocumentConverterService");

    private static final String INPUT_FILE_PATH = "file_to_convert.ext";

    public static void main(String[] args) throws Exception {
        String filePath = args.length == 1 ? args[0] : INPUT_FILE_PATH;

        Path source = Paths.get(filePath);
        String fileName = stripExtension(source.getFileName().toString());
        String fileExt = extensionOf(source.getFileName().toString());

        DocumentConverterService_Service service =
                new DocumentConverterService_Service(new URL(WSDL_URL), SERVICE_QNAME);
        DocumentConverterService dcs = service.getBasicHttpBindingDocumentConverterService();

        ObjectFactory of = new ObjectFactory();

        OpenOptions openOptions = new OpenOptions();
        openOptions.setOriginalFileName(of.createOpenOptionsOriginalFileName(fileName));
        openOptions.setFileExtension(of.createOpenOptionsFileExtension(fileExt));

        ConversionSettings settings = new ConversionSettings();
        settings.setQuality(ConversionQuality.OPTIMIZE_FOR_PRINT);
        settings.setRange(ConversionRange.ALL_DOCUMENTS);
        settings.getFidelity().add("Full");
        settings.setFormat(OutputFormat.PDF);

        byte[] input = Files.readAllBytes(source);
        System.out.println("Converting " + source);

        byte[] converted = dcs.convert(input, openOptions, settings);

        Path output = source.resolveSibling(fileName + ".pdf");
        Files.write(output, converted);
        System.out.println("Wrote " + output);
    }

    private static String stripExtension(String name) {
        int dot = name.lastIndexOf('.');
        return dot < 0 ? name : name.substring(0, dot);
    }

    private static String extensionOf(String name) {
        int dot = name.lastIndexOf('.');
        return dot < 0 ? "" : name.substring(dot + 1);
    }
}

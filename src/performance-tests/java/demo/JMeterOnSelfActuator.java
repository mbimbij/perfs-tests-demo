package demo;

import org.apache.jmeter.control.LoopController;
import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.protocol.http.sampler.HTTPSampler;
import org.apache.jmeter.report.config.ConfigurationException;
import org.apache.jmeter.report.config.ReportGeneratorConfiguration;
import org.apache.jmeter.report.dashboard.ExportException;
import org.apache.jmeter.report.dashboard.HtmlTemplateExporter;
import org.apache.jmeter.report.dashboard.ReportGenerator;
import org.apache.jmeter.report.processor.SampleContext;
import org.apache.jmeter.reporters.ResultCollector;
import org.apache.jmeter.reporters.Summariser;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.threads.SetupThreadGroup;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.HashTree;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

class JMeterOnSelfActuator {

    @Test
    void canReachActuatorOnLocalhost() throws Exception {
        URL url = new URL("http://localhost:8080/actuator/health");
        StandardJMeterEngine jMeterEngine = new StandardJMeterEngine();
        JMeterUtils.loadJMeterProperties("src/performance-tests/resources/jmeter.properties");
        Path outdir = Paths.get("results");

        // Http Sampler
        HTTPSampler httpSampler = new HTTPSampler();
        httpSampler.setDomain(url.getHost());
        httpSampler.setPort(url.getPort());
        httpSampler.setPath(url.getPath());
        httpSampler.setMethod("GET");

        // Loop Controller
        TestElement loopCtrl = new LoopController();
        ((LoopController) loopCtrl).setLoops(1);
        ((LoopController) loopCtrl).addTestElement(httpSampler);
        ((LoopController) loopCtrl).setFirst(true);

        // Thread Group
        SetupThreadGroup threadGroup = new SetupThreadGroup();
        threadGroup.setNumThreads(1);
        threadGroup.setRampUp(1);
        threadGroup.setSamplerController((LoopController) loopCtrl);

        // Test Plan
        TestPlan testPlan = new TestPlan("MY TEST PLAN");

        // Parameters
        HashTree hashTree = new HashTree();
        hashTree.add("testPlan", testPlan);
        hashTree.add("loopCtrl", testPlan);
        hashTree.add("threadGroup", testPlan);
        hashTree.add("httpSampler", testPlan);

        jMeterEngine.configure(hashTree);
        jMeterEngine.run();

        Files.createDirectories(outdir);
        Path resultFile = outdir.resolve("output.jtl");
        Summariser summer = null;
        String summariserName = JMeterUtils.getPropDefault("summariser.name", "summary");
        if (summariserName.length() > 0) {
            summer = new Summariser(summariserName);
        }
        ResultCollector resultCollector = new ResultCollector(summer);
        resultCollector.setFilename(resultFile.toString());
        hashTree.add(testPlan, resultCollector);

    }

    private void exportHtmlResults() throws IOException, ConfigurationException, ExportException {
        HtmlTemplateExporter htmlTemplateExporter = new HtmlTemplateExporter();
        htmlTemplateExporter.setName("html");
        SampleContext sampleContext = new SampleContext();
        File file = new File("report.txt");
        Properties reportGenerationProperties = new Properties();
        FileReader fileReader = new FileReader(new File("src/performance-tests/resources/htmlReportGenerator.properties"));
        reportGenerationProperties.load(fileReader);
        ReportGeneratorConfiguration reportGeneratorConfiguration = ReportGeneratorConfiguration.loadFromProperties(reportGenerationProperties);
        htmlTemplateExporter.export(sampleContext, file, reportGeneratorConfiguration);
    }

}

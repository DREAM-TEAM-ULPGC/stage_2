package es.ulpgc.benchmark;

import es.ulpgc.catalog.MetadataExtractor;
import es.ulpgc.datamart.MetadataParser;
import es.ulpgc.inverted_index.FileProcessor;
import es.ulpgc.inverted_index.InvertedIndex;
import org.openjdk.jmh.annotations.*;

import java.io.File;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 10, time = 1)
@Fork(1)
public class IndexerBenchmark {

    private String sampleBookPath;
    private String sampleHeaderContent;
    private String sampleBodyContent;
    
    @Setup
    public void setup() {
        sampleBookPath = "datalake/20251008/14/1342";
        
        sampleHeaderContent = """
            Title: Pride and Prejudice
            Author: Jane Austen
            Language: en
            Release Date: 1998-06-01
            """;
        
        StringBuilder body = new StringBuilder();
        for (int i = 0; i < 10000; i++) {
            body.append("This is sample text for benchmarking purposes. ");
        }
        sampleBodyContent = body.toString();
    }

    @Benchmark
    public void benchmarkMetadataExtraction() {
        MetadataExtractor extractor = new MetadataExtractor();
        extractor.extractMetadata(sampleHeaderContent);
    }

    @Benchmark
    public void benchmarkMetadataParsing() {
        MetadataParser parser = new MetadataParser();
        parser.parse(sampleHeaderContent);
    }

    @Benchmark
    public void benchmarkTextTokenization() {
        FileProcessor processor = new FileProcessor();
        processor.tokenize(sampleBodyContent);
    }

    @Benchmark
    public void benchmarkIndexConstruction() {
        InvertedIndex index = new InvertedIndex();
        FileProcessor processor = new FileProcessor();
        
        var tokens = processor.tokenize(sampleBodyContent);
        index.addDocument(1342, tokens);
    }

    @Benchmark
    public void benchmarkFullIndexingPipeline() {
        MetadataExtractor extractor = new MetadataExtractor();
        FileProcessor processor = new FileProcessor();
        InvertedIndex index = new InvertedIndex();
        
        var metadata = extractor.extractMetadata(sampleHeaderContent);
        
        var tokens = processor.tokenize(sampleBodyContent);
        
        index.addDocument(1342, tokens);
    }

    public static void main(String[] args) throws Exception {
        org.openjdk.jmh.Main.main(args);
    }
}
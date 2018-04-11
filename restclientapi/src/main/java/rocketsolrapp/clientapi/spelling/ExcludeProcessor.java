package rocketsolrapp.clientapi.spelling;

import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.core.SolrCore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class ExcludeProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(ExcludeProcessor.class);

    private AtomicReference<Directory> directory = new AtomicReference<>();

    public void init(NamedList config, SolrCore core) {
        final String filePath = (String) config.get("filePath");
        if (filePath == null) return;
        final Path file = Paths.get(filePath);
        if (!Files.exists(file)) return;
        try {
            buildLuceneIndexFrom(Files.newBufferedReader(file));
        } catch (Exception ex) {
            LOG.error(ex.getMessage());
        }
    }

    public Set<String> findProhibitedTerms(List<String> terms) throws Exception {
        if (directory.get() == null) return Collections.emptySet();
        IndexReader reader = DirectoryReader.open(directory.get());
        final Set<String> result = new HashSet<>();
        try {
            for (String tokenText : terms) {
                long total = reader.docFreq(new Term("words", tokenText));
                if (total != 0) {
                    result.add(tokenText);
                }
            }
        } catch (Exception ex) {
            LOG.error(ex.getMessage());
        }

        return result;
    }

    private void buildLuceneIndexFrom(BufferedReader reader) {
        final Directory indexDir = new RAMDirectory();
        try {
            IndexWriterConfig conf = new IndexWriterConfig(new SimpleAnalyzer());
            IndexWriter writer = new IndexWriter(indexDir, conf);
            reader.lines().forEach( line -> {
                Document document = new Document();
                document.add(new Field("words", line, StringField.TYPE_NOT_STORED));
                try {
                    writer.addDocument(document);
                } catch (Exception ex) {
                    LOG.error(ex.getMessage());
                }
            });
            writer.commit();
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        directory.set(indexDir);
    }
}

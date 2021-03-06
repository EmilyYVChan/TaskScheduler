package se306.team7.Digraph;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import se306.team7.utility.FileUtilities;
import se306.team7.utility.IFileUtilities;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DigraphParserTest {

    private IDigraphParser _digraphParser;
    private IFileUtilities _fileUtilities;

    public DigraphParserTest() {

        _fileUtilities = mock(FileUtilities.class);
        try {
            when(_fileUtilities.createFileReader("testfile.dot"))
                    .thenReturn(new BufferedReader(new StringReader(
                            "digraph \"validDigraph\" {\n" +
                            "a \t [Weight=2];\n" +
                            "b \t [Weight=3];\n" +
                            "a −> b \t [Weight=1];\n" +
                            "c \t [Weight=3];\n" +
                            "a −> c \t [Weight=2];\n" +
                            "d \t [Weight=2];\n" +
                            "b −> d \t [Weight=2];\n" +
                            "c −> d \t [Weight=1];\n" +
                            "}")));
        }catch(FileNotFoundException e){
            e.printStackTrace();
        }

        _digraphParser = new DigraphParser(_fileUtilities);
    }

    @Test
    public void ParseDigraph_ReturnsDigraph_WhenInputValid() {

        // Arrange
        DigraphBuilder db = new DigraphBuilder();
        db.setName("validDigraph");
        db.addNode("a", 2);
        db.addNode("b", 3);
        db.addLink("a", "b", 1);
        db.addNode("c", 3);
        db.addLink("a", "c", 2);
        db.addNode("d", 2);
        db.addLink("b", "d", 2);
        db.addLink("c", "d", 1);
        IDigraph desiredDigraph = db.build();

        // Act
        IDigraph testDigraph = null;
        try {
            testDigraph = _digraphParser.parseDigraph("testfile.dot");
        } catch (IOException e) {
            e.printStackTrace();
        }

        assertEquals(testDigraph, desiredDigraph);

    }
}

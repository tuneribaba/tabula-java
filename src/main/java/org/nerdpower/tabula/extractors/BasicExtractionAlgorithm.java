package org.nerdpower.tabula.extractors;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Arrays;

import org.nerdpower.tabula.Line;
import org.nerdpower.tabula.Page;
import org.nerdpower.tabula.Rectangle;
import org.nerdpower.tabula.Ruling;
import org.nerdpower.tabula.Table;
import org.nerdpower.tabula.TextChunk;
import org.nerdpower.tabula.TextElement;

public class BasicExtractionAlgorithm implements ExtractionAlgorithm {
    
    private List<Ruling> verticalRulings = null;
    
    public BasicExtractionAlgorithm() {
    }
    
    public BasicExtractionAlgorithm(List<Ruling> verticalRulings) {
        this.verticalRulings = verticalRulings;
    }
    
    public List<Table> extract(Page page, List<Float> verticalRulingPositions) {
        List<Ruling> verticalRulings = new ArrayList<Ruling>(verticalRulingPositions.size());
        for (Float p: verticalRulingPositions) {
            verticalRulings.add(new Ruling((float) page.getTop(), (float) p, 0.0f, (float) page.getHeight()));
        }
        this.verticalRulings = verticalRulings;
        return this.extract(page);
    }

    @Override
    public List<Table> extract(Page page) {
        
        List<TextElement> textElements = page.getText();
        
        if (textElements.size() == 0) {
            return Arrays.asList(new Table[] { Table.EMPTY });
        }
        
        List<TextChunk> textChunks = this.verticalRulings == null ? TextElement.mergeWords(page.getText()) : TextElement.mergeWords(page.getText(), this.verticalRulings);
        List<Line> lines = TextChunk.groupByLines(textChunks);
        List<Float> columns = null;
        
        if (this.verticalRulings != null) {
            Collections.sort(this.verticalRulings, new Comparator<Ruling>() {
                @Override
                public int compare(Ruling arg0, Ruling arg1) {
                    return Double.compare(arg0.getLeft(), arg1.getLeft());
                }
            });
            columns = new ArrayList<Float>(this.verticalRulings.size());
            for (Ruling vr: this.verticalRulings) {
                columns.add(vr.getLeft());
            }
        }
        else {
            columns = columnPositions(lines);
        }
        
        // Build a list of Rulings from detected 'invisible separators' in the page
        List<Ruling> rulings = new ArrayList<Ruling>();

        rulings.add(new Ruling(new Point2D.Float(page.getLeft(), page.getTop()), new Point2D.Float(page.getLeft(), page.getBottom()), false));
        for (float c: columns) {
            rulings.add(new Ruling(new Point2D.Float(c, page.getTop()), new Point2D.Float(c, page.getBottom()), false));
        }
        
        rulings.add(new Ruling(new Point2D.Float(page.getLeft(), lines.get(0).getTop()), new Point2D.Float(page.getRight(), lines.get(0).getTop()), false));
        for (Line l: lines.subList(1, lines.size())) {
            rulings.add(new Ruling(new Point2D.Float(page.getLeft(), l.getTop()), new Point2D.Float(page.getRight(), l.getTop()), false));
        }
        rulings.add(new Ruling(new Point2D.Float(page.getLeft(), lines.get(lines.size() - 1).getBottom()), 
                new Point2D.Float(page.getRight(), lines.get(lines.size() - 1).getBottom()), false));
        
        SpreadsheetExtractionAlgorithm sea = new SpreadsheetExtractionAlgorithm();
        List<? extends Table> tables = sea.extract(page, rulings);
        for (Table t: tables) {
            t.setExtractionAlgorithm(this);
        }
        return (List<Table>) tables;

    }
    
    @Override
    public String toString() {
        return "basic";
    }
    
    
    /**
     * @param lines must be an array of lines sorted by their +top+ attribute
     * @return a list of column boundaries (x axis)
     */
    public static List<java.lang.Float> columnPositions(List<Line> lines) {

        List<Rectangle> regions = new ArrayList<Rectangle>();
        for (TextChunk tc: lines.get(0).getTextElements()) {
            if (tc.isSameChar(Line.WHITE_SPACE_CHARS)) { 
                continue; 
            }
            Rectangle r = new Rectangle();
            r.setRect(tc);
            regions.add(r);
        }
        
        for (Line l: lines.subList(1, lines.size())) {
            List<TextChunk> lineTextElements = new ArrayList<TextChunk>();
            for (TextChunk tc: l.getTextElements()) {
                if (!tc.isSameChar(Line.WHITE_SPACE_CHARS)) { 
                    lineTextElements.add(tc);
                }
            }
            
            for (Rectangle cr: regions) {

                List<TextChunk> overlaps = new ArrayList<TextChunk>();
                for (TextChunk te: lineTextElements) {
                    if (cr.horizontallyOverlaps(te)) {
                        overlaps.add(te);
                    }
                }
                
                for (TextChunk te: overlaps) {
                    cr.merge(te);
                }
                
                lineTextElements.removeAll(overlaps);
            }
            
            for (TextChunk te: lineTextElements) {
                Rectangle r = new Rectangle();
                r.setRect(te);
                regions.add(r);
            }
        }
        
        List<java.lang.Float> rv = new ArrayList<java.lang.Float>();
        for (Rectangle r: regions) {
            rv.add((float) r.getRight());
        }
        
        Collections.sort(rv);
        
        return rv;
        
    }

}

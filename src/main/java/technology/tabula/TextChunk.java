package technology.tabula;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Collections;
import java.util.HashMap;


@SuppressWarnings("serial")
public class TextChunk extends RectangularTextContainer<TextElement> implements HasText { 
    public static final TextChunk EMPTY = new TextChunk(0,0,0,0);
    List<TextElement> textElements = new ArrayList<TextElement>();
    
    public TextChunk(float top, float left, float width, float height) {
        super(top, left, width, height);
    }
    
    public TextChunk(TextElement textElement) {
        super(textElement.y, textElement.x, textElement.width, textElement.height);
        this.add(textElement);
    }
    
    public TextChunk(List<TextElement> textElements) {
        this(textElements.get(0));
        for (int i = 1; i < textElements.size(); i++) {
            this.add(textElements.get(i));
        }
    }

    // used for "normalizing" RTL languages.
    // TEMPORARY TODO REMOVE etc.
    public void reverseTextElements(){
        Collections.reverse(this.textElements);
    }


    // I hate Java so bad.
    private HashMap<Byte, String> directionalities = new HashMap<Byte, String>() {{   // BCT = bidirectional character type
        put(java.lang.Character.DIRECTIONALITY_ARABIC_NUMBER, "LTR");               // Weak BCT    "AN" in the Unicode specification.
        put(java.lang.Character.DIRECTIONALITY_BOUNDARY_NEUTRAL, "None");            // Weak BCT    "BN" in the Unicode specification.
        put(java.lang.Character.DIRECTIONALITY_COMMON_NUMBER_SEPARATOR, "LTR");     // Weak BCT    "CS" in the Unicode specification.
        put(java.lang.Character.DIRECTIONALITY_EUROPEAN_NUMBER, "LTR");             // Weak BCT    "EN" in the Unicode specification.
        put(java.lang.Character.DIRECTIONALITY_EUROPEAN_NUMBER_SEPARATOR, "LTR");   // Weak BCT    "ES" in the Unicode specification.
        put(java.lang.Character.DIRECTIONALITY_EUROPEAN_NUMBER_TERMINATOR, "LTR");  // Weak BCT    "ET" in the Unicode specification.
        put(java.lang.Character.DIRECTIONALITY_LEFT_TO_RIGHT, "LTR");              // Strong BCT  "L" in the Unicode specification.
        put(java.lang.Character.DIRECTIONALITY_LEFT_TO_RIGHT_EMBEDDING, "LTR");     // Strong BCT  "LRE" in the Unicode specification.
        put(java.lang.Character.DIRECTIONALITY_LEFT_TO_RIGHT_OVERRIDE, "LTR");      // Strong BCT  "LRO" in the Unicode specification.
        put(java.lang.Character.DIRECTIONALITY_NONSPACING_MARK, "None");             // Weak BCT    "NSM" in the Unicode specification.
        put(java.lang.Character.DIRECTIONALITY_OTHER_NEUTRALS, "None");              // Neutral BCT "ON" in the Unicode specification.
        put(java.lang.Character.DIRECTIONALITY_PARAGRAPH_SEPARATOR, "None");         // Neutral BCT "B" in the Unicode specification.
        put(java.lang.Character.DIRECTIONALITY_POP_DIRECTIONAL_FORMAT, "None");      // Weak BCT    "PDF" in the Unicode specification.
        put(java.lang.Character.DIRECTIONALITY_RIGHT_TO_LEFT, "RTL");              // Strong BCT  "R" in the Unicode specification.
        put(java.lang.Character.DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC, "RTL");       // Strong BCT  "AL" in the Unicode specification.
        put(java.lang.Character.DIRECTIONALITY_RIGHT_TO_LEFT_EMBEDDING, "RTL");    // Strong BCT  "RLE" in the Unicode specification.
        put(java.lang.Character.DIRECTIONALITY_RIGHT_TO_LEFT_OVERRIDE, "RTL");     // Strong BCT  "RLO" in the Unicode specification.
        put(java.lang.Character.DIRECTIONALITY_SEGMENT_SEPARATOR, "RTL");          // Neutral BCT "S" in the Unicode specification.
        put(java.lang.Character.DIRECTIONALITY_UNDEFINED, "None");                   // Undefined BCT.
        put(java.lang.Character.DIRECTIONALITY_WHITESPACE, "None");                  // Neutral BCT "WS" in the Unicode specification.
    }};

    /** Splits a TextChunk into N TextChunks, where each chunk is of a single directionality, and
        then reverse the RTL ones.
        what we're doing here is *reversing* the Unicode bidi algorithm
        in the language of that algorithm, each chunk is a (maximal) directional run.
        We attach whitespace to the beginning of non-RTL
    **/
    public TextChunk groupByDirectionality(Boolean isLtrDominant) {
        if (this.getTextElements().size() <= 0) {
            throw new IllegalArgumentException();
        }

        ArrayList<ArrayList<TextElement>> chunks = new ArrayList<ArrayList<TextElement>>();
        ArrayList<TextElement> buff = new ArrayList<TextElement>();
        String buffDirectionality = "None"; // the directionality of the characters in buff;

        for(TextElement te: this.getTextElements()){
            //TODO: we might need to loop over the textelement characters
            //      is it possible for a textelement to contain multiple characters?
            
            // System.out.println(te.getText() + " is " + Character.getDirectionality(te.getText().charAt(0) ) + " " + directionalities.get(Character.getDirectionality(te.getText().charAt(0) )));
            if(buff.size() == 0){
                buff.add(te);
                buffDirectionality = directionalities.get(Character.getDirectionality(te.getText().charAt(0)));
            }else{
                if(buffDirectionality == "None"){
                    buffDirectionality = directionalities.get(Character.getDirectionality(te.getText().charAt(0)));
                }
                String teDirectionality = directionalities.get(Character.getDirectionality(te.getText().charAt(0)));

                if(teDirectionality == buffDirectionality || teDirectionality == "None") {
                    if ( Character.getDirectionality(te.getText().charAt(0) ) == java.lang.Character.DIRECTIONALITY_WHITESPACE && (buffDirectionality != (isLtrDominant ? "LTR" : "RTL") ) ){
                        buff.add(0, te);
                    }else{
                        buff.add(te);
                    }
                }else{
                    // finish this chunk
                    if (buffDirectionality == "RTL"){
                        Collections.reverse(buff);
                    }
                    chunks.add(buff);

                    // and start a new one
                    buffDirectionality = directionalities.get(Character.getDirectionality(te.getText().charAt(0)));
                    buff = new ArrayList<TextElement>();
                    buff.add(te);
                }
            }
        }
        if (buffDirectionality == "RTL"){
            Collections.reverse(buff);
        }
        chunks.add(buff);
        ArrayList<TextElement> everything = new ArrayList<TextElement>();
        if(!isLtrDominant){
            Collections.reverse(chunks);
        }
        for(ArrayList<TextElement> group : chunks){
            everything.addAll(group);
        }
        return new TextChunk(everything);
    }


    public TextChunk merge(TextChunk other) {
        super.merge(other);
        return this;
    }

    public void add(TextElement textElement) {
        this.textElements.add(textElement);
        this.merge(textElement);
    }
    
    public void add(List<TextElement> textElements) {
        for (TextElement te: textElements) {
            this.add(te);
        }
    }

    public List<TextElement> getTextElements() {
        return textElements;
    }
    
    public String getText() {
        if (this.textElements.size() == 0) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (TextElement te: this.textElements) {
            sb.append(te.getText());
        }
        return sb.toString();
    }
    
    @Override
    public String getText(boolean useLineReturns) {
        // TODO Auto-generated method stub
        return null;
    }

    
    /**
     * Returns true if text contained in this TextChunk is the same repeated character
     */
    public boolean isSameChar(Character c) {
        return isSameChar(new Character[] { c });
    }
    
    public boolean isSameChar(Character[] c) {
        String s = this.getText();
        List<Character> chars = Arrays.asList(c);
        for (int i = 0; i < s.length(); i++) {
            if (!chars.contains(s.charAt(i))) { return false; }
        }
        return true;
    }
    
    /** Splits a TextChunk in two, at the position of the i-th TextElement
     */
    public TextChunk[] splitAt(int i) {
        if (i < 1 || i >= this.getTextElements().size()) {
            throw new IllegalArgumentException();
        }
        
        TextChunk[] rv = new TextChunk[] {
                new TextChunk(this.getTextElements().subList(0, i)),
                new TextChunk(this.getTextElements().subList(i, this.getTextElements().size()))
        };
        return rv;
    }
    
    /**
     * Removes runs of identical TextElements in this TextChunk
     * For example, if the TextChunk contains this string of characters: "1234xxxxx56xx"
     * and c == 'x' and minRunLength == 4, this method will return a list of TextChunk
     * such that: ["1234", "56xx"]
     */
    public List<TextChunk> squeeze(Character c, int minRunLength) {
        Character currentChar, lastChar = null;
        int subSequenceLength = 0, subSequenceStart = 0;
        TextChunk[] t;
        List<TextChunk> rv = new ArrayList<TextChunk>();
        
        for (int i = 0; i < this.getTextElements().size(); i++) {
            TextElement textElement = this.getTextElements().get(i);
            currentChar = textElement.getText().charAt(0); 
            if (lastChar != null && currentChar.equals(c) && lastChar.equals(currentChar)) {
                subSequenceLength++;
            }
            else {
                if (((lastChar != null && !lastChar.equals(currentChar)) || i + 1 == this.getTextElements().size()) && subSequenceLength >= minRunLength) {

                    if (subSequenceStart == 0 && subSequenceLength <= this.getTextElements().size() - 1) {
                        t = this.splitAt(subSequenceLength);
                    }
                    else {
                        t = this.splitAt(subSequenceStart);
                        rv.add(t[0]);
                    }
                    rv.addAll(t[1].squeeze(c, minRunLength)); // Lo and behold, recursion.
                    break;

                }
                subSequenceLength = 1;
                subSequenceStart = i;
            }
            lastChar = currentChar;
        }
        
        
        if (rv.isEmpty()) { // no splits occurred, hence this.squeeze() == [this]
            if (subSequenceLength >= minRunLength && subSequenceLength < this.textElements.size()) {
                TextChunk[] chunks = this.splitAt(subSequenceStart); 
                rv.add(chunks[0]);
            }
            else {
                rv.add(this);
            }
        }
        
        return rv;

    }
    
    
    
    @Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((textElements == null) ? 0 : textElements.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		TextChunk other = (TextChunk) obj;
		if (textElements == null) {
			if (other.textElements != null)
				return false;
		} else if (!textElements.equals(other.textElements))
			return false;
		return true;
	}

	public static boolean allSameChar(List<TextChunk> textChunks) {
        char first = textChunks.get(0).getText().charAt(0);
        for (TextChunk tc: textChunks) {
            if (!tc.isSameChar(first)) return false;
        }
        return true;
    }
    
    public static List<Line> groupByLines(List<TextChunk> textChunks) {
        List<Line> lines = new ArrayList<Line>();

        if (textChunks.size() == 0) {
            return lines;
        }

        float bbwidth = Rectangle.boundingBoxOf(textChunks).width;
        
        Line l = new Line();
        l.addTextChunk(textChunks.get(0));
        textChunks.remove(0);
        lines.add(l);

        Line last = lines.get(lines.size() - 1);
        for (TextChunk te: textChunks) {
            if (last.verticalOverlapRatio(te) < 0.1) {
                if (last.width / bbwidth > 0.9 && TextChunk.allSameChar(last.getTextElements())) {
                    lines.remove(lines.size() - 1);
                }
                lines.add(new Line());
                last = lines.get(lines.size() - 1);
            }
            last.addTextChunk(te);
        }
        
        if (last.width / bbwidth > 0.9 && TextChunk.allSameChar(last.getTextElements())) {
            lines.remove(lines.size() - 1);
        }
        
        List<Line> rv = new ArrayList<Line>(lines.size());
        
        for (Line line: lines) {
            rv.add(Line.removeRepeatedCharacters(line, ' ', 3));
        }
        
        return rv;
    }

}

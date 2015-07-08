import java.util.regex.*;

public class Search {

    private int BACKWARD_BLOCK_SIZE = 200;
    private int BACKWARD_OVERLAPPING = 20;

    private Matcher myFwdMatcher;
    private Matcher myBkwMatcher;
    private String  mySearch;
    private int myCurrOffset;
    private boolean myRegexp;
    private CharSequence mySearchData;

    public Search(CharSequence searchData) {
        mySearchData = searchData;
        mySearch = "";
        myCurrOffset = 0;
        myRegexp = true;
        clear();
    }

    public void clear() {
        myFwdMatcher = null;
        myBkwMatcher = null;
    }

    public String getSearch() {
        return mySearch;
    }

    public void setSearch(String toSearch) {
        if ( !mySearch.equals(toSearch) ) {
            mySearch = toSearch;
            clear();
        }
    }

    public void setData(CharSequence searchData) {
        mySearchData = searchData;
        clear();
    }

    public void setStartOffset(int startOffset) {
        if (myCurrOffset != startOffset) {
            myCurrOffset = startOffset;
            clear();
        }
    }

    public boolean isRegexp() {
        return myRegexp;
    }

    public void setRegexp(boolean regexp) {
        if (myRegexp != regexp) {
            myRegexp = regexp;
            clear();
        }
    }

    public int searchForward() {

        if (mySearchData != null) {

            boolean found;

            if (myFwdMatcher == null)
            {
                // if it's a new search, start from beginning
                String searchPattern = myRegexp ? mySearch : Pattern.quote(mySearch);
                myFwdMatcher = Pattern.compile(searchPattern, Pattern.CASE_INSENSITIVE).matcher(mySearchData);
                try {
                    found = myFwdMatcher.find(myCurrOffset);
                } catch (IndexOutOfBoundsException e) {
                    found = false;
                }
            }
            else
            {
                // continue searching
                found = myFwdMatcher.hitEnd() ? false : myFwdMatcher.find();
            }

            if (found) {
                MatchResult result = myFwdMatcher.toMatchResult();
                return onMatchResult(result);
            }
        }
        return -1;
    }

    public int searchBackward() {

        if (mySearchData != null) {

            myFwdMatcher = null;

            if (myBkwMatcher == null)
            {
                // if it's a new search, create a new matcher
                String searchPattern = myRegexp ? mySearch : Pattern.quote(mySearch);
                myBkwMatcher = Pattern.compile(searchPattern, Pattern.CASE_INSENSITIVE).matcher(mySearchData);
            }

            MatchResult result = null;
            boolean startOfInput = false;
            int start = myCurrOffset;
            int end = start;

            while (result == null && !startOfInput)
            {
                start -= BACKWARD_BLOCK_SIZE;
                if (start < 0) {
                    start = 0;
                    startOfInput = true;
                }
                try {
                    myBkwMatcher.region(start, end);
                } catch (IndexOutOfBoundsException e) {
                    break;
                }
                while ( myBkwMatcher.find() ) {
                    result = myBkwMatcher.toMatchResult();
                }
                end = start + BACKWARD_OVERLAPPING; // depending on the size of the pattern this could not be enough
                                                    //but how can you know the size of a regexp match beforehand?
            }

            return onMatchResult(result);
        }
        return -1;
    }

    private int onMatchResult(MatchResult result) {
        if (result != null) {
            myCurrOffset = result.start();
            return myCurrOffset;
        }
        return -1;
    }
}
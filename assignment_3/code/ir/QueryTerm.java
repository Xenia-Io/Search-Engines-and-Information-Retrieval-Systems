package ir;

class QueryTerm {

    double weight;

    String term;

    QueryTerm(String term){
        this(term, 1.0);
    }

    QueryTerm(String term, double weight) {

        this.term = term;
        this.weight = weight;
    }

    QueryTerm copy() {

        return new QueryTerm(term, weight);
    }

    String getTerm() {

        return term;
    }

    String getRegex() {

        return "^" + term.replace("", "\\w") + "$";
    }

    String getRegexTerm() {

        return "^" + term + "$";
    }

    double getWeight() {

        return weight;
    }

    boolean isWildCard() {

        return term.contains("*");
    }

}
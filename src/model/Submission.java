package model;

public class Submission
{
    public String submitId;
    public String userName;
    public String sourceCode;
    public String language;

    public Submission(String submitId, String userName, String sourceCode, String language)
    {
        this.submitId = submitId;
        this.userName = userName;
        this.sourceCode = sourceCode;
        this.language = language;
    }
}
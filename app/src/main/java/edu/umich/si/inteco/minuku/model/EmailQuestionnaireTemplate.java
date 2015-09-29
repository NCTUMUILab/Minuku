package edu.umich.si.inteco.minuku.model;

/**
 * Created by Armuro on 7/3/14.
 */
public class EmailQuestionnaireTemplate extends QuestionnaireTemplate{

    protected String[] mRecipients;
    protected String mSubject = "Subject";

    public EmailQuestionnaireTemplate(){
        super();

    }

    public EmailQuestionnaireTemplate(int id, String title, int study_id, String type) {
        super(id, title, study_id, type);

    }

    public String[] getRecipients() {
        return mRecipients;
    }

    public void setRecipients(String[] recipients) {
        this.mRecipients = recipients;
    }

    public String getSubject() {
        return mSubject;
    }

    public void setSubject(String subject) {
        this.mSubject = subject;
    }
}

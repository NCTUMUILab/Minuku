package edu.umich.si.inteco.minuku.model.Questionnaire;

/**
 * Created by Armuro on 7/3/14.
 */
public class EmailQuestionnaire extends EmailQuestionnaireTemplate{

    protected int _templateId = -1;
    protected long _generatedTime=-1;
    protected long _attendedTime=-1;
    protected long _submittedTime=-1;
    protected boolean _submitted= false;


    public EmailQuestionnaire(long generated_time, int study_id, int template_id){
        super();
        _generatedTime = generated_time;
        _studyId = study_id;
        _templateId = template_id;

    }

    public EmailQuestionnaire(int id, String title, int study_id, String type) {
        super(id, title, study_id, type);

    }

    public int getTemplateId(){
        return _templateId;
    }

    public void setTemplateId(int template_id){
        _templateId = template_id;
    }

    public long getGeneratedTime(){
        return _generatedTime;
    }

    public void setGeneratedTime(long time){
        _generatedTime = time;
    }

    public long getAttendedTime(){
        return _attendedTime;
    }

    public void setAttendedTime(long time){
        _attendedTime = time;
    }

    public long getSubmittedTime(){
        return _submittedTime;
    }

    public void setSubmittedTime(long time){
        _submittedTime = time;
    }

    public boolean isSubmitted(){
        return _submitted;
    }

    public void setSubmitted(boolean flag){
        _submitted = flag;
    }


}

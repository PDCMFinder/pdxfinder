package org.pdxfinder.commands.usi;

import java.io.IOException;

/*
 * Created by csaba on 21/11/2018.
 */
public class USIHelper {




    public static String createSubmission(String token, String submissionsApiTemplatedUrl, String submitterEmail, String teamName) throws IOException {
        String submissionApiUrl = submissionsApiTemplatedUrl.replace("{teamName}", teamName);
        //String content = TestJsonUtils.getSubmissionJson(submitterEmail, teamName);
        //return submitAndGetResponse(token, submissionApiUrl, content);
        return "";
    }






}

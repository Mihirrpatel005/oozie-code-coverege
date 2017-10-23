package com.mastercard.pclo;

import java.io.IOException;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.apache.oozie.client.OozieClient;
import org.apache.oozie.client.OozieClientException;
import org.apache.oozie.client.WorkflowAction;
import org.apache.oozie.client.WorkflowJob;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;

@Service
public class OozieJob {

	@Value("${url}")
	public String url;

	@Value("${bashDir}")
	public String bashDir;
	
	@Value("${projectName}")
	public String projectName;
	
	@Autowired
	private LineNumber linenumber;

	public void getOozielist() throws OozieClientException, IOException, SAXException {

		OozieClient oozieClient = openConnection(url);

		if (oozieClient != null) {

			List<WorkflowJob> wf = getOozieJobList(oozieClient);

			for (int list_job = 0; list_job < 50; list_job++) {

				String jobId = wf.get(list_job).getId();
				String workFlowAppPath = oozieClient.getJobInfo(jobId).getAppPath();
				List<WorkflowAction> actionList = oozieClient.getJobInfo(jobId).getActions();

				System.out.println("\nid ::" + list_job + " ::" + jobId);
				System.out.println("path :: " + list_job + " ::" + workFlowAppPath);
				System.out.println("action list :: " + list_job + " ::" + actionList + "\n");

				List<WorkflowAction> workFlowActionList = (ArrayList<WorkflowAction>) oozieClient.getJobInfo(jobId).getActions();

				Matcher m = regex(workFlowActionList);

				List<List<String>> exeBranch = new ArrayList<List<String>>();

				List<String> l1 = new ArrayList<String>();

				List<String> exeActionNodeList = new ArrayList<String>();

				while (m.find()) {

					String word1 = m.group(1);
					if (word1.contentEquals(":start:")) {
						word1 = word1.replace(":", "");
					}
					if (!word1.equalsIgnoreCase("start") && !word1.equalsIgnoreCase("Kill")
							&& !word1.equalsIgnoreCase("End")) {
						// System.out.println(" exec node name ----" +word1);
						exeActionNodeList.add(word1);
					}
					l1.add(word1);
					// System.out.print(word1.toString() + "\n");
				}
				exeBranch.add(l1);

				System.out.println("\n\n============================\n" + "Executed Branch ::  " + list_job + " ::" + l1
						+ "\n===============================");

				System.out.println(">>>> file path : " + fileLocaction(workFlowAppPath));
				String file_name = fileLocaction(workFlowAppPath);
				if (!file_name.contains("false")) {
					linenumber.oozieCoverageInput(fileLocaction(workFlowAppPath), l1);
				}
			}
		} else {
			System.out.println("====================================");
			System.out.println("     oozie connection problem");
			System.out.println("====================================");
		}
	}

	public OozieClient openConnection(String oozieUrl) {
		return new OozieClient(oozieUrl);
	}

	public List<WorkflowJob> getOozieJobList(OozieClient client) throws ConnectException, OozieClientException {
		System.out.println("open connection call");
		return client.getJobsInfo("status=SUCCEEDED");
	}

	public Matcher regex(List<WorkflowAction> actionList) {

		String actionlist = actionList.toString();
		String s1 = "name\\[";
		String s2 = "(.*?[a-z,-]+.*?)";
		String s3 = "(\\])";

		Pattern p = Pattern.compile(s1 + s2 + s3, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
		return p.matcher(actionlist);
	}

	public String fileLocaction(String filePath) {

		if (!filePath.contains(projectName)) {
			return "false";
		}
		String filepath[] = filePath.split(projectName);
		if (filePath.length() > 0) {
			return bashDir + "" + filepath[1];
		} else {
			return "false";
		}
	}

}

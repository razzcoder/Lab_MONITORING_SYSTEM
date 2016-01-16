package controller;

import java.beans.beancontext.BeanContext;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.catalina.Session;

import service.LabService;

import bean.Dependent;
import bean.Hcp;
import bean.HcpLogin;
import bean.LSP;
import bean.Login;
import bean.Patient;
import bean.PatientSecurity;
import bean.Search_bean;
import bean.View_Schedule;
import bean.lsplogin;

/**
 * Servlet implementation class LabControllerServlet
 */
@WebServlet("/LabControllerServlet")
public class LabControllerServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
   
    public LabControllerServlet() {
        super();
       
    }

	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
	}

	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
				
		String action=request.getParameter("action");
													
													
		
								//This one belongs to dependent servlet
								
								
		//PAtient Registration Himself
		
		
		if(action.equals("register"))
		{
			
			//registration of the patient will be done here
		String firstName = request.getParameter("firstname");
		String lastName = request.getParameter("lastname");
		String gender = request.getParameter("gender");
		String age =request.getParameter("age");
		String address = request.getParameter("address");
		String contactNo = request.getParameter("contact");
		String healthInsuranceNo =request.getParameter ("healthInsuranceNo");
		
		Patient bean = new Patient();
		bean.setFirstName(firstName);
		bean.setLastName(lastName);
		bean.setGender(gender);
		bean.setAge(age);
		bean.setAddress(address);
		bean.setContactNo(contactNo);
		bean.setHealthInsuranceNo(healthInsuranceNo);
		bean.setHCP_ID(null);
		bean.setBill("0");
		
		LabService service = new LabService();
		String password=service.generatePassword();
		
		String patient_id=service.addPatient(bean);
		
		request.setAttribute("pid",patient_id);
		request.setAttribute("password",password );
		RequestDispatcher rd=request.getRequestDispatcher("login.jsp");
		rd.forward(request, response);
		}
		
		
		
		
		
		if("registerhcp".equals(action))
		{

			//this will register the HCP and after successful registration will redirect  it to login page
			LabService hcps=new LabService();
			Hcp hcp=new Hcp();
			String contact=request.getParameter("contact");
			hcp.setHcpName(request.getParameter("hcpname"));
			System.out.println("Hcp name is "+request.getParameter("hcpname"));
			hcp.setHcpLicenseNumber(request.getParameter("licensenumber"));
			System.out.println("licensenumber is "+request.getParameter("licensenumber"));
			hcp.setAddress(request.getParameter("address"));
			System.out.println("Address is "+request.getParameter("address"));
			hcp.setContact(request.getParameter("contact"));
			System.out.print("HCp contact in controller class "+request.getParameter("contact"));
			hcp.setEmergencyContact(request.getParameter("altcontact"));
			System.out.println("Alternate Contact number is "+request.getParameter("altcontact"));
			System.out.println("before emaail");
			hcp.setEmailId(request.getParameter("email"));
			System.out.println("after emaail"+request.getParameter("email"));
			String userIDPwd = null;
			HcpLogin hl=new HcpLogin();
			try {
				String password=hcps.generatePassword();
				hl = hcps.createHcp(hcp);
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
			request.setAttribute("hcpid", hl.getHcpid());
			request.setAttribute("password",hl.getPassword());
			RequestDispatcher requestdispatcher=request.getRequestDispatcher("login.jsp");
			requestdispatcher.forward(request, response);
			
		}
			
		
		//Patient Logins here 
		
		
		if(action.equals("login_patient"))
		{
			//login of the patient will be done here
			String pid=request.getParameter("pid");
			String password=request.getParameter("password");
			
			LabService ps=new LabService();
			String msg=ps.loginPatient(pid,password);
			
			request.setAttribute("msg",msg);
			if(msg!=null)
			{
				HttpSession session=request.getSession();
				session.setAttribute("pid", msg);
			RequestDispatcher rs=request.getRequestDispatcher("Patientpanel.jsp");
			rs.forward(request, response);
			}
			else 
			{
				RequestDispatcher rs=request.getRequestDispatcher("login_error.jsp");
				rs.forward(request, response);
			}
		}
		
		//Hcp Logins here
		
		if("login_hcp".equals(action))
		{
			//this will check whether the user's login details  is correct or not
			LabService hcps=new LabService();
			Login login=new Login();
			login.setLoginID(request.getParameter("login"));
			//String loginId=request.getParameter("login");
			System.out.println(login.getLoginID());
			login.setPassword(request.getParameter("password"));
			//String password=request.getParameter("password");
			//Login success=null;
			//success=hcps.validateLogin(login.getLoginID(),login.getPassword());
			String success=null;
			success=hcps.validateLogin(login);

			if(success!=null)
			{
				//means right user
				HttpSession sd=request.getSession(false);
				sd.setAttribute("hcpid", success);
				RequestDispatcher rd=request.getRequestDispatcher("hcpPanel.jsp");
				rd.forward(request, response);
			}
			else 
			{
				
				RequestDispatcher rd=request.getRequestDispatcher("login_error.jsp");
				rd.forward(request, response);
				//means wrong user
			}
		
		}
		
		//Patient Forgets his/her Password
		
		if(action.equals("forget_patient"))
		{
			//check whether security question and security answer matches exaclty with that in the database
			LabService ps=new LabService();
			String pid=request.getParameter("patient_id");
			String sq=request.getParameter("security_question");
			String sa=request.getParameter("security_answer");
			PatientSecurity psec=new PatientSecurity();
			psec.setSecurityAnswer(sa);
			psec.setSecurityQuestion(sq);
			String msg=ps.checkPatient(psec,pid);
			if(msg.equals("ok"))
			{
				request.setAttribute("ppid",pid);
				RequestDispatcher rd=request.getRequestDispatcher("forgetchangepassword.jsp");
				rd.forward(request, response);
				
			}
			else
			{
				
				RequestDispatcher rd=request.getRequestDispatcher("error.jsp");
				rd.forward(request, response);
			}
			
			
			
		}
		
		///When Patient Gives Right security answer so he will be prompted to change his password
		
		if("forgetchangepassword".equals(action))
		{
			
			String pid=request.getParameter("ppid");
			String newpwd=request.getParameter("new_password");
			PatientSecurity psec=new PatientSecurity();
			psec.setNewPwd(newpwd);
			LabService ps=new LabService();
			{
				String msg=ps.changepassword(pid,psec);
				request.setAttribute("msg",msg);
				if(msg.equals("ok"))
				{
				RequestDispatcher rd=request.getRequestDispatcher("login.jsp");
				rd.forward(request, response);
				}
				else
				{
					RequestDispatcher rd=request.getRequestDispatcher("forgetchangepassword.jsp");
					rd.forward(request, response);
				}
			}
			
		}
		//When HCP Forgets his/her password
		
		if(action.equals("forget_hcp"))
		{
			//check whether security question and security answer matches exaclty with that in the database
			LabService ps=new LabService();
			String pid=request.getParameter("patient_id");
			String sq=request.getParameter("security_question");
			String sa=request.getParameter("security_answer");
			PatientSecurity psec=new PatientSecurity();
			psec.setSecurityAnswer(sa);
			psec.setSecurityQuestion(sq);
			String msg=ps.checkHCP(psec,pid);
			if(msg.equals("ok"))
			{
				request.setAttribute("ppid",pid);
				RequestDispatcher rd=request.getRequestDispatcher("forgethcpchangepassword.jsp");
				rd.forward(request, response);
				
			}
			else
			{
				
				RequestDispatcher rd=request.getRequestDispatcher("error.jsp");
				rd.forward(request, response);
			}
			
			
			
		}
		//When HCP Gives Correct Security Question and answers
		
		if("forgethcpchangepassword".equals(action))
		{
			
			String pid=request.getParameter("ppid");
			String newpwd=request.getParameter("new_password");
			PatientSecurity psec=new PatientSecurity();
			psec.setNewPwd(newpwd);
			LabService ps=new LabService();
	
			{
				String msg=ps.hcpchangepassword(pid,psec);
				request.setAttribute("msg",msg);
				if(msg.equals("ok"))
				{
				RequestDispatcher rd=request.getRequestDispatcher("login.jsp");
				rd.forward(request, response);
				}
				else
				{
					RequestDispatcher rd=request.getRequestDispatcher("forgetchangepassword.jsp");
					rd.forward(request, response);
				}
			}
		
		

		}
		
		//View the Patient Details or Profile by Patient
		
		
		if(action.equals("viewPatient"))
		{
			
			HttpSession session=request.getSession();
					String pid=(String)session.getAttribute("pid");
					System.out.println("prinint gfrom session pid"+pid);
			LabService ps=new LabService();
			Patient p=new Patient();
			p=ps.viewPatients(pid);
			System.out.println("printing from view patient method"+p.getMedicalComplaint());
			request.setAttribute("pat",p);
			
			RequestDispatcher rd=request.getRequestDispatcher("viewPatient.jsp");
			rd.forward(request, response);
			//get the arrayList of that particular patient
		}
		
		
		
		//Display the Patient details to update their values in form fields
		
		if(action.equals("viewPatientupdate"))
		{
			
			HttpSession session=request.getSession();
					String pid=(String)session.getAttribute("pid");
					System.out.println("prinint gfrom session pid"+pid);
			LabService ps=new LabService();
			Patient p=new Patient();
			p=ps.viewPatients(pid);
			System.out.println("printing from view patient method"+p.getPatientID());
			request.setAttribute("pat",p);
			RequestDispatcher rd=request.getRequestDispatcher("updatePatient.jsp");
			rd.forward(request, response);
		}
		
		//After displaying the patient details it should be updated in database from this 
		
		if(action.equals("updatePatient"))
		{
			String firstName = request.getParameter("firstname");
			String lastName = request.getParameter("lastname");
			String gender = request.getParameter("gender");
			String age =request.getParameter("age");
			String address = request.getParameter("address");
			String contactNo = request.getParameter("contact");
			String healthInsuranceNo =request.getParameter ("isInsuranceCover");		
			
			Patient bean = new Patient();
			bean.setFirstName(firstName);
			bean.setLastName(lastName);
			bean.setGender(gender);
			bean.setAge(age);
			bean.setAddress(address);
			bean.setContactNo(contactNo);
			bean.setHealthInsuranceNo(healthInsuranceNo);
			bean.setHCP_ID(null);
			bean.setBill("0");
			LabService ps=new LabService();
			HttpSession s6=request.getSession();
			String pid=	(String)s6.getAttribute("pid");
			String msg=ps.updatePatient(bean,pid);
			if(msg.equals("ok"))
			{
				
				request.setAttribute("message", "successupdate");
				RequestDispatcher rd=request.getRequestDispatcher("modalforaction.jsp");
				rd.forward(request, response);
			}
			else
			{
				
				request.setAttribute("message", "errorupdate");
				RequestDispatcher rd=request.getRequestDispatcher("updatePatient.jsp");
				rd.forward(request, response);
			
			}
			//update the details of the patient
			//call the service method here
			
		}
		
		
		
		//Add the dependent details or register dependent
		
		if(action.equals("AddDependent"))
		{
			
			HttpSession session=request.getSession();
			String p_id=(String)session.getAttribute("pid");
			System.out.println(p_id);
			LabService ds=new LabService();
			
			
			Dependent d=new Dependent();
			 String first_name=request.getParameter("First_name");
			 String last_name=request.getParameter("Last_name");
			 String relationship=request.getParameter("Relationship");
			int age=Integer.parseInt(request.getParameter("Age").toString());
			 String address=request.getParameter("Address");
			 String gender=request.getParameter("Gender");
			 String test_health_number=request.getParameter("Health_number");
			 int health_number=0;
				if((test_health_number)!=null)
				{
					health_number=Integer.parseInt(test_health_number);
				}
			 String phone_number=request.getParameter("Phone_number");
			 d.setFirst_name(first_name);
			 d.setLast_name(last_name);
			 d.setRelationship(relationship);
			 d.setAge(age);
			 d.setAddress(address);
			 d.setGender(gender);
			 d.setHealth_number(health_number);
			 d.setPhone_number(phone_number);
		
		
		
			String d_id=null;
			try {
				//use session here for passing p_id
				d_id=ds.addDependent(p_id,d);
				
				
			} catch (SQLException e) {
				e.printStackTrace();
			}
			if(d_id!=null){
				request.setAttribute("message", "successupdate");
				RequestDispatcher rs=request.getRequestDispatcher("modalforaction.jsp");
				rs.forward(request, response);
			}
			else{
				request.setAttribute("message", "errorupdate");
				RequestDispatcher rs=request.getRequestDispatcher("modalforaction.jsp");
				rs.forward(request, response);
				
			}
			
			
			
		}
		
		//View the dependent Details here
		
		if(action.equals("ViewDependent"))
		{
			HttpSession session=request.getSession();
			
			
			LabService ds=new LabService();
			
			
			ArrayList<Dependent> al=null;
			String pid=(String)session.getAttribute("pid");
			System.out.println("Patient pid printing"+pid);
			al=ds.viewDependent(pid);
			request.setAttribute("alist",al);
			RequestDispatcher rd = request.getRequestDispatcher("viewdependent.jsp");
			rd.forward(request, response);
		}
		
		//Update the Dependents Details
		
		
		if(action.equals("UpdateDependent")){
			//System.out.println("aaaye ho yha pe");
			
						HttpSession session=request.getSession();
						String p_id=(String)session.getAttribute("pid");
						System.out.println(p_id);
						LabService ds=new LabService();
					 Dependent d=new Dependent();
					 String dep_id=request.getParameter("dept_id");
					 String first_name=request.getParameter("First_name");
					 String last_name=request.getParameter("Last_name");
					 String relationship=request.getParameter("Relationship");
					 int age=Integer.parseInt(request.getParameter("Age").toString());
					 String address=request.getParameter("Address");
					 String gender=request.getParameter("Gender");
					 String test_health_number=request.getParameter("Health_number");
					 int health_number=0;
						if((test_health_number)!=null)
						{
							health_number=Integer.parseInt(test_health_number);
						}
					 String phone_number=request.getParameter("Phone_number");
					 d.setFirst_name(first_name);
					 d.setLast_name(last_name);
					 d.setRelationship(relationship);
					 d.setAge(age);
					 d.setAddress(address);
					 d.setGender(gender);
					 d.setHealth_number(health_number);
					 d.setPhone_number(phone_number);
					 d.setDep_id(dep_id);
		
					int count1=0;
					try {
						count1=ds.updateDependent(d,p_id);
						request.setAttribute("message", "successupdate");
						RequestDispatcher rd=request.getRequestDispatcher("modalforaction.jsp");
						rd.forward(request, response);
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						request.setAttribute("message", "errorupdate");
						RequestDispatcher rd=request.getRequestDispatcher("modalforaction.jsp");
						rd.forward(request, response);
					}
					if(count1!=0){
						System.out.println("total" +count1 +"Dependents updated");
						
						
					}
		}
		
		//change the patient of the logged in Patient
		
		if("changepassword".equals(action))
		{
					String oldpwd=request.getParameter("old_password");
					String newpwd=request.getParameter("new_password");
					PatientSecurity psec=new PatientSecurity();
					psec.setNewPwd(newpwd);
					psec.setOldPwd(oldpwd);
					//changing Patient password here
					LabService ps=new LabService();
					HttpSession hn=request.getSession();
					String pid=(String)hn.getAttribute("pid");
					String checkmsg=ps.checkoldpassword(pid,oldpwd);
					if(checkmsg.equals("ok"))
					{
						String msg=ps.changepassword(pid,psec);
						request.setAttribute("msg",msg);
						if(msg.equals("ok"))
						{
						request.setAttribute("message", "successupdate");
						RequestDispatcher rd=request.getRequestDispatcher("modalforaction.jsp");
						rd.forward(request, response);
						}
						else
						{
							request.setAttribute("message", "errorupdate");
							RequestDispatcher rd=request.getRequestDispatcher("modalforaction.jsp");
							rd.forward(request, response);
						}
					}
					else
					{
						//old password doesnt matches with the database one
						request.setAttribute("message", "mismatch");
						RequestDispatcher rd=request.getRequestDispatcher("modalforaction.jsp");
						rd.forward(request, response);
					}
		
		}
		
		//Set the security question and answer for patient
		
		if(action.equals("setsecurity"))
		{
					//setting the security question will be done here
					String sq=request.getParameter("security_question");
					String sa=request.getParameter("security_answer");
					HttpSession sf=request.getSession();
					String pid=(String)sf.getAttribute("pid");
					
					PatientSecurity psec=new PatientSecurity();
					psec.setSecurityAnswer(sa);
					psec.setSecurityQuestion(sq);
					LabService ps=new LabService();
					String msg=ps.setSecurity(psec,pid);
					if(msg.equals("ok"))
					{
						request.setAttribute("message", "successupdate");
						RequestDispatcher rd=request.getRequestDispatcher("modalforaction.jsp");
						rd.forward(request, response);
					}
					else
					{
						request.setAttribute("message", "errorupdate");
						RequestDispatcher rd=request.getRequestDispatcher("modalforaction.jsp");
						rd.forward(request, response);
					}
			
			
		}
		
		//Delete the Patient Profile by Patient himself
		
		if("deletePatient".equals(action))
		{
			
			LabService ps=new LabService();
			HttpSession sf=request.getSession();
			String pid=(String)sf.getAttribute("pid");
			String msg=ps.deletePatient(pid);
			if(msg.equals("ok"))
			{
				RequestDispatcher rd=request.getRequestDispatcher("index.jsp");
				sf.invalidate();
				rd.forward(request, response);
			}
			else
			{
				RequestDispatcher rd=request.getRequestDispatcher("patientPanel.jsp");
				rd.forward(request, response);
				
			}
			
		}
		
		//View the HCP profile By HCP himself 
		
		if("view_hcp".equals(action))
		{
					//this shows the details of the HCP or profile of the HCP
					LabService hcps=new LabService();
						ArrayList<Hcp> hcpList =new 	ArrayList<Hcp>();
						try {
							HttpSession sd=request.getSession();
							String hcpid=(String)sd.getAttribute("hcpid");
							
							hcpList = hcps.viewHcp(hcpid);
						} catch (SQLException e) {
							
							e.printStackTrace();
						}
						System.out.println("flow in back");
						request.setAttribute("hcpList", hcpList);
						RequestDispatcher reqDispatcher = request.getRequestDispatcher("viewhcp.jsp");
						reqDispatcher.forward(request, response);
		}
		
		
		
		
		//show the details to Update the HCP profile by HCP himself 
		
		if("UpdatehcpProfile".equalsIgnoreCase(action))
		{
			
			//this will get the update request from the HCP and show the update form along with details
			System.out.println("in update profile");
			LabService hcps=new LabService();
			Hcp hcp=new Hcp();
			Login login=new Login();
			HttpSession session=request.getSession();
			System.out.println(session.getAttribute("hcpid"));
			String loginid=session.getAttribute("hcpid").toString();
			hcp=hcps.updatehcpProfile(loginid);
			System.out.println(hcp.getHcpName());
			RequestDispatcher rd=request.getRequestDispatcher("UpdatehcpProfile.jsp?hcpname="+hcp.getHcpName()+"&loginid="+hcp.getId()+"&licensenumber="+hcp.getHcpLicenseNumber()+"&address="+hcp.getAddress()+"&contactnumber="+hcp.getContact()+"&emergencycontnum="+hcp.getEmergencyContact()+"&email="+hcp.getEmailId());
			rd.forward(request, response);
			//session.invalidate();
		}
		
		
		//Update the HCP profile by HCP himself
		
		if("Updatenew".equalsIgnoreCase(action))
		{
					//this will actually update the HCP details using update query in dao class
					System.out.println("In new update");
					LabService hcps=new LabService();
					Hcp hcp=new Hcp();
					hcp.setHcpName(request.getParameter("hcpname"));
					hcp.setHcpLicenseNumber(request.getParameter("licensenumber"));
					hcp.setAddress(request.getParameter("address"));
					hcp.setContact(request.getParameter("contactnumber"));
					hcp.setEmergencyContact(request.getParameter("emergencycontnum"));
					System.out.println("before emaail");
					hcp.setEmailId(request.getParameter("email"));
					HttpSession session=request.getSession();
					System.out.println(session.getAttribute("hcpid"));
					String loginid=session.getAttribute("hcpid").toString();
					String success=hcps.updateNew(hcp,loginid);
					System.out.println(success);
					if(success.equals("ok"))
					{
					request.setAttribute("message", "successupdate");
					RequestDispatcher rd=request.getRequestDispatcher("hcpmodalforaction.jsp");
					rd.forward(request, response);			
					}
					else
					{
						request.setAttribute("message", "errorupdate");
						RequestDispatcher rd=request.getRequestDispatcher("hcpmodalforaction.jsp");
						rd.forward(request, response);	
					}
		}
		
		//Add patient By HCP
		
		ArrayList<Patient>patientList1=new ArrayList<Patient>();
		//ArrayList<Patient>patientList2=new ArrayList<Patient>();
		if(action.equalsIgnoreCase("registerpatientviahcp"))
		{
			
						HttpSession session=request.getSession();
						System.out.println(session.getAttribute("hcpid"));
						String loginid=session.getAttribute("hcpid").toString();
							
						LabService ps=new LabService();
						String firstName = request.getParameter("firstname");
						String lastName = request.getParameter("lastname");
						String gender = request.getParameter("gender");
						String age =request.getParameter("age");
						String address = request.getParameter("address");
						String contactNo = request.getParameter("contact");
						String healthInsuranceNo =request.getParameter ("healthInsuranceNo");
						String medical_complaint = request.getParameter("medical_complaint");
						String referredDoctor = request.getParameter("referredDoctor");
						String bill=request.getParameter("bill");
						Patient patient = new Patient();
						patient.setFirstName(firstName);
						patient.setLastName(lastName);
						patient.setGender(gender);
						patient.setAge(age);
						patient.setAddress(address);
						patient.setContactNo(contactNo);
						patient.setHealthInsuranceNo(healthInsuranceNo);
						patient.setMedicalComplaint(medical_complaint );
						patient.setReferDoctor(referredDoctor);
						patient.setBill(bill);
						String success=ps.registerPatientviahcp(patient,loginid);
						System.out.println(success);
						request.setAttribute("hcp_p_id", success.substring(0, (success.length()-8)));
						request.setAttribute("hcp_p_password", success.substring((success.length()-8),success.length()));
						RequestDispatcher rd=request.getRequestDispatcher("patientcredits.jsp");
						rd.forward(request, response);
	}
		//View  the Patient Profile for updating by HCP
		
		if("UpdatePatientProfile".equalsIgnoreCase(action))
		{
			HttpSession session=request.getSession();
			System.out.println(session.getAttribute("hcpid"));
			String loginid=session.getAttribute("hcpid").toString();
			//used for displaying all the patient 
			ArrayList<Patient>patientList=new ArrayList<Patient>();
			System.out.println("in update profile");
			LabService ps=new LabService();
			Patient patient=new Patient();
			System.out.println(session.getAttribute("hcpid"));
			loginid=session.getAttribute("hcpid").toString();
			patientList=ps.updatePatientProfile(loginid);
			for(Patient p :patientList)
			{
				System.out.println(p.getPatientID());
			}
			
			HttpSession  se=request.getSession();
			
			se.setAttribute("patientList1", patientList);
			RequestDispatcher reqDispatcher = request.getRequestDispatcher("viewPatientResult.jsp");
			reqDispatcher.forward(request, response);
			patientList1=patientList;	
			
		}
		//Display the form for Updating the patient Details via HCP
		
		
				if("UpdateNewPatientProfile".equalsIgnoreCase(action))
				{
					String choice=request.getParameter("choice");
					String patientid=request.getParameter("search");
					System.out.println("patient for updating profile is "+patientid);
					HttpSession sj=request.getSession();
					ArrayList<Patient> patientList5 = (ArrayList<Patient>) sj.getAttribute("patientList1");
					String searchResult=searchPatient(patientList5,patientid);
					if(searchResult.equalsIgnoreCase("true")){
					Patient patient=new Patient();
					LabService ps=new LabService();
					//for(Patient patient:patientList)
					patient=ps.updateNewProfilePatient(patientid);
					//System.out.println(patient.getMedicalComplaint());
					RequestDispatcher rd1=request.getRequestDispatcher("updatePatientviaHCP.jsp?patientid="+patient.getPatientID()+"&firstname="+patient.getFirstName()+"&lastname="+patient.getLastName()+"&gender="+patient.getGender()+"&age="+patient.getAge()+"&address="+patient.getAddress()+"&contact="+patient.getContactNo()+"&healthInsuranceNo="+patient.getHealthInsuranceNo()+"&medical_complaint="+patient.getMedicalComplaint()+"&referredDoctor="+patient.getReferDoctor()+"&bill="+patient.getBill());
					rd1.forward(request, response);
					}
					else
					{
						response.sendRedirect("norecordsmodal.jsp");
						System.out.println("You cannot update");
					}
					}
				
				
				
				//finally update the Patient profile by HCP after displaying the details of the Patient
				
				
				
				if("finalupdate".equalsIgnoreCase(action))
				{
					System.out.println("In new update");
					LabService ps=new LabService();
					Patient patient=new Patient();
					String ppid=(String)request.getAttribute("ppid");
					patient.setPatientID(request.getParameter("patientid"));
					System.out.println(request.getParameter("patientid"));
					System.out.println(request.getParameter("firstname"));
					patient.setFirstName(request.getParameter("firstname"));
					patient.setLastName(request.getParameter("lastname"));
					patient.setGender(request.getParameter("gender"));
					patient.setAddress(request.getParameter("address"));
					patient.setContactNo(request.getParameter("contact"));
					patient.setAge(request.getParameter("age"));
					patient.setHealthInsuranceNo(request.getParameter("healthInsuranceNo"));
					patient.setBill(request.getParameter("bill"));
					patient.setMedicalComplaint(request.getParameter("medical_complaint"));
					patient.setReferDoctor(request.getParameter("referredDoctor"));
					System.out.println("before emaail");
					System.out.println("I am getting patient Id as"+request.getParameter("patientid"));
					String success=ps.finalUpdate(patient,patient.getPatientID());
					System.out.println(success);
					if(success.equals("ok"))
					{
					request.setAttribute("message", "successupdate");
					RequestDispatcher rd=request.getRequestDispatcher("hcpmodalforaction.jsp");
					rd.forward(request, response);	
					
					}
					else
					{
						request.setAttribute("message", "errorupdate");
						RequestDispatcher rd=request.getRequestDispatcher("hcpmodalforaction.jsp");
						rd.forward(request, response);	
					}
				}
				
				//Displaying the patients to HCP while deleting the Patient
				
				if("deletePatientviahcp".equalsIgnoreCase(action))
				{
					
					HttpSession session=request.getSession();
					System.out.println(session.getAttribute("hcpid"));
					String loginid=session.getAttribute("hcpid").toString();
					
					System.out.println("I am here in deletePatient Profile");
					ArrayList<Patient> patientList=new ArrayList<Patient>();
					System.out.println("in update profile");
					LabService ps=new LabService();
					Patient patient=new Patient();
					System.out.println("hcpID    "+session.getAttribute("hcpid"));
					loginid=session.getAttribute("hcpid").toString();
					patientList=ps.deletePatientviaHCP(loginid);
					for(Patient p :patientList)
					{
						System.out.println("In patientList"+p.getPatientID());
						
					}
					HttpSession sm=request.getSession();
					sm.setAttribute("patientList", patientList);
					//
				
						//response.sendRedirect("deleteview.jsp");
					RequestDispatcher reqDispatcher = request.getRequestDispatcher("deleteview.jsp");
					reqDispatcher.forward(request, response);
					
			
				}
				
				
				//Deleting the Patient by HCP by entering either the Patient ID or phone number
				
				
				if("deleteNewPatientviahcp".equalsIgnoreCase(action))
				{
					
					
					HttpSession session=request.getSession();
					System.out.println(session.getAttribute("hcpid"));
					String loginid=session.getAttribute("hcpid").toString();
					
					
					
					String patientid=request.getParameter("searchdel");
					
					HttpSession sm=request.getSession();
					ArrayList<Patient> patientList2 = (ArrayList<Patient>) session.getAttribute("patientList");
					System.out.println(patientid);
					System.out.println("SIZE  "+patientList2.size());
					String searchResult=searchPatientdel(patientList2,patientid);
					System.out.println(searchResult);
							if(searchResult.equalsIgnoreCase("true"))
							{
								Patient patient=new Patient();
								LabService ps=new LabService();
								String success=ps.deleteNewPatient(patientid);
								System.out.println(success);
								RequestDispatcher rd=request.getRequestDispatcher("deletePatientmodalviahcp.jsp");
								rd.forward(request, response);
							}
							else
							{
								System.out.println("Patient not found");
								RequestDispatcher rd=request.getRequestDispatcher("norecordsmodal.jsp");
								rd.forward(request, response);
							}
					}
				
				
				//Changing Password of HCP via HCP
				

				if("changepasswordviahcp".equals(action))
				{
					String oldpwd=request.getParameter("old_password");
					String newpwd=request.getParameter("new_password");
					PatientSecurity psec=new PatientSecurity();
					psec.setNewPwd(newpwd);
					psec.setOldPwd(oldpwd);
					//changing Patient password here
					LabService ps=new LabService();
					HttpSession hn=request.getSession();
					String hcpid=(String)hn.getAttribute("hcpid");
					String checkmsg=ps.checkoldpasswordviahcp(hcpid,oldpwd);
					if(checkmsg.equals("ok"))
					{
						String msg=ps.changepasswordviahcp(hcpid,psec);
						request.setAttribute("msg",msg);
						if(msg.equals("ok"))
						{
							request.setAttribute("message", "successupdate");
							RequestDispatcher rd=request.getRequestDispatcher("hcpmodalforaction.jsp");
							rd.forward(request, response);
						}
						else
						{
							request.setAttribute("message", "errorupdate");
							RequestDispatcher rd=request.getRequestDispatcher("hcpmodalforaction.jsp");
							rd.forward(request, response);
						}
					}
					else
					{
						//old password doesnt matches with the database one
						request.setAttribute("message", "mismatch");
						RequestDispatcher rd=request.getRequestDispatcher("hcpmodalforaction.jsp");
						rd.forward(request, response);
					}
				}
				
				
				//Setting the Security Question of the HCP and Security Answer for the HCP
				
				if(action.equals("setsecurityviahcp"))
				{
					//setting the security question will be done here
					String sq=request.getParameter("security_question");
					String sa=request.getParameter("security_answer");
					HttpSession sf=request.getSession();
					String hcpid=(String)sf.getAttribute("hcpid");
					
					PatientSecurity psec=new PatientSecurity();
					psec.setSecurityAnswer(sa);
					psec.setSecurityQuestion(sq);
					LabService ps=new LabService();
					String msg=ps.setSecurityviahcp(psec,hcpid);
					if(msg.equals("ok"))
					{
						request.setAttribute("message","successupdate");
						RequestDispatcher rd=request.getRequestDispatcher("hcpmodalforaction.jsp");
						rd.forward(request, response);
					}
					else
					{
						RequestDispatcher rd=request.getRequestDispatcher("error.jsp");
						rd.forward(request, response);
					}
					
					
				}
				
				//Searching for the LAB Service Provider from the Search Form
				
				
				if(action.equals("search_form"))
				{
					
					String search=request.getParameter("search");
					LabService patient_service=new LabService();
					ArrayList<Search_bean> arsearch=new ArrayList<Search_bean>();
					
					arsearch=patient_service.searchPatient(search);
					request.setAttribute("arsearch", arsearch);
					RequestDispatcher rd=request.getRequestDispatcher("search_result.jsp");
					rd.forward(request, response);
				}
				
				
				//after searching we can view the schedule here ..this feature is only for Patient and HCP only ...not for guest users
				
				
				if(action.equals("view_schedule"))
				{
					
					//view the schedule on clicking view schedule link
					//version 1.0
					String city=request.getParameter("city");
					String lab_service_name=request.getParameter("lab_service_name");
					String test_name=request.getParameter("test_name");
					String test_code=request.getParameter("test_code");
					View_Schedule v=new View_Schedule();
					v.setTest_code(test_code);
					v.setCity(city);
					v.setLab_service_provider_name(lab_service_name);
					v.setTest_name(test_name);
					ArrayList<View_Schedule> vs=new ArrayList<View_Schedule>();
					LabService patient_service=new LabService();
					vs=patient_service.view_schedule(test_name,lab_service_name,city);
				
					request.setAttribute("vs", vs);
					RequestDispatcher rd=request.getRequestDispatcher("view_schedule.jsp");
					rd.forward(request, response);
					
					
				}
				
				
				
				//Sort the Result for the LAb Service Providers so that he can View the sorted Result based on cost ..Only for HCP it is available
				
				
				if(action.equals("sort_results"))
				{
					LabService patient_service=new LabService();
					ArrayList<Search_bean> vs=new ArrayList<Search_bean>();
					
					vs=patient_service.sortedResults();
					request.setAttribute("vt", vs);
					RequestDispatcher rd=request.getRequestDispatcher("view_sorted_schedule.jsp");
					rd.forward(request, response);
				}
				
				
				//LsP registration is done here only
				if(action.equals("lspregister"))
				{
					String lspname=request.getParameter("lspname");
					String email=request.getParameter("lspemail");
					String contact=request.getParameter("lspcontact");
					String address=request.getParameter("lspaddress");
					LSP lsp=new LSP();
					lsp.setLsp_name(lspname);
					lsp.setLsp_address(address);
					lsp.setLsp_contact(contact);
					lsp.setLsp_email(email);
					LabService lspservice=new LabService();
					String msg=lspservice.LspService(lsp);
					if(msg.equals("ok"))
					{
						
						request.setAttribute("username", "lsp1");
						request.setAttribute("password", "12345");
						RequestDispatcher rd=request.getRequestDispatcher("lsp_login.jsp");
						rd.forward(request, response);
						
					}
					else
					{
						RequestDispatcher rd=request.getRequestDispatcher("error.jsp");
						rd.forward(request, response);
						
						
					}
					
		
					
				}

				if(action.equals("login_lsp"))
				{
					
					
					String login=request.getParameter("");
					String password=request.getParameter("");
					lsplogin ls=new lsplogin();
					ls.setLsplogin(login);
					ls.setPassword(password);
					LabService ls2=new LabService();
					
					
					
					
					
					
				}
		}
			
			

			
			
			
			
			
			
			
			
			
			
			
			
			//Search method used for searching the Patient while updating its details via HCP
			
			
			
			
			private String searchPatient(ArrayList<Patient> patientList,String search) {
				
				String succ="false";
				for(Patient patient:patientList)
				{
					if(patient.getPatientID().equalsIgnoreCase(search)){
						succ="true";
					}
					
				}
				return succ;
			}
			
			
			//Search method used for searching patient while  deleting the patient via HCP 
			
			
			
			private String searchPatientdel(ArrayList<Patient> patientList,String search) {
				
				String succ="false";
				for(Patient patient:patientList)
				{
					System.out.println(patient.getLastName());
					if(patient.getPatientID().equalsIgnoreCase(search)){
						succ="true";
						return succ;
					}
					
				}
				return succ;
			}
		}


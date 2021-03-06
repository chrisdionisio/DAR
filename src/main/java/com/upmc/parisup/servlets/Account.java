package com.upmc.parisup.servlets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.upmc.parisup.DAO.AbstractDAOFactory;
import com.upmc.parisup.DAO.Factory;
import com.upmc.parisup.DAO.DAOImpl.SchoolDAOImpl;
import com.upmc.parisup.DAO.DAOImpl.SelectedSchoolDAOImpl;
import com.upmc.parisup.DAO.DAOImpl.UserDAOImpl;
import com.upmc.parisup.business.School;
import com.upmc.parisup.business.SelectedSchool;
import com.upmc.parisup.business.User;
import com.upmc.parisup.services.SchoolService;
import com.upmc.parisup.services.UserService;
import com.upmc.parisup.services.Util;

public class Account extends HttpServlet {
	private static final long serialVersionUID = -5677200504573287154L;

	public Account() {
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		// Add all schools into combobox
		List<School> schools = ((SchoolDAOImpl) AbstractDAOFactory.getFactory(Factory.MYSQL_DAO_FACTORY).getSchoolDAO())
				.getAll();

		UserService us = new UserService();

		request.setAttribute("schools", schools);

		// User infos
		User user = us.getCurrentUser(request);
		request.setAttribute("user", user);

		// Schools selected
		if (user != null) {
			List<SelectedSchool> selectedSchools = ((SelectedSchoolDAOImpl) AbstractDAOFactory
					.getFactory(Factory.MYSQL_DAO_FACTORY).getSelectedSchoolDAO()).getByUserID(user.getId());
			request.setAttribute("selectedSchools", selectedSchools);
		}

		request.getRequestDispatcher("WEB-INF/signup.jsp").forward(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		UserService us = new UserService();
		ArrayList<String> errors = new ArrayList<String>();
		JSONObject json = new JSONObject();

		User currentUser = us.getCurrentUser(request);
		User user = us.getUser(request, errors, currentUser.getEmail());

		// If no errors, then modify
		if (user != null) {
			String password = request.getParameter("password");

			// Update user informations in DB
			currentUser.update(user, !password.isEmpty());
			((UserDAOImpl) AbstractDAOFactory.getFactory(Factory.MYSQL_DAO_FACTORY).getUserDAO()).update(currentUser);

			// Delete the previous selected schools in DB
			List<SelectedSchool> previousSchools = ((SelectedSchoolDAOImpl) AbstractDAOFactory
					.getFactory(Factory.MYSQL_DAO_FACTORY).getSelectedSchoolDAO()).getByUserID(currentUser.getId());
			for (int i = 0; i < previousSchools.size(); i++)
				((SelectedSchoolDAOImpl) AbstractDAOFactory.getFactory(Factory.MYSQL_DAO_FACTORY)
						.getSelectedSchoolDAO()).delete(previousSchools.get(i));

			// Add the new selected schools
			new SchoolService().addNewSelectedSchools(request, currentUser.getId());

			ObjectMapper mapper = new ObjectMapper();
			String userToJson = mapper.writeValueAsString(user);
			json.put("user", userToJson);
		}

		json.put("success", user != null);
		json.put("message", String.join("\n", errors));

		Util.sendJSON(response, json);
	}
}
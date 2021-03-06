package com.upmc.parisup.DAO;

import java.util.List;

import com.upmc.parisup.business.Rating;

/**
 * 
 * Rating DAO
 *
 */
public interface RatingDAO extends DAO<Rating> {

	public List<Rating> getAllByDateDescAndSchoolID(Long idSchool);
	
}
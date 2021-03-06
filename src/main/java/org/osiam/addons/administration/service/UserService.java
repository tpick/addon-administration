package org.osiam.addons.administration.service;

import javax.inject.Inject;

import org.osiam.addons.administration.model.session.GeneralSessionData;
import org.osiam.addons.administration.model.session.PagingInformation;
import org.osiam.client.OsiamConnector;
import org.osiam.client.oauth.AccessToken;
import org.osiam.client.query.Query;
import org.osiam.client.query.QueryBuilder;
import org.osiam.resources.scim.SCIMSearchResult;
import org.osiam.resources.scim.UpdateUser;
import org.osiam.resources.scim.User;
import org.springframework.stereotype.Component;

/**
 * This class contains all logic about handling users.
 */
@Component
public class UserService {

	@Inject
	private GeneralSessionData sessionData;

	@Inject
	private OsiamConnector connector;

	/**
	 * Search for existing Users.
	 *
	 * @see OsiamConnector#searchUsers(Query, AccessToken)
	 *
	 * @param query
	 *        Containing the query to execute.
	 * @param limit
	 *        The maximum number of returned resources.
	 * @param offset
	 *        The (1-based) index of the first resource.
	 * @param orderBy
	 *        The attribute to sort the resulting resources by.
	 * @param ascending
	 *        The sort direction of the resulting resources.
	 * @param attributes
	 *        List of attributes to return.
	 * @return A SCIMSearchResult Containing a list of all found Users.
	 */
	public SCIMSearchResult<User> searchUser(String query, Integer limit, Long offset, String orderBy,
			Boolean ascending, String attributes) {
		QueryBuilder qb = new QueryBuilder();
		qb.filter(query);
		qb.attributes(attributes);
		qb.count(limit == null ? 0 : limit);
		qb.startIndex(offset == null ? 0 : offset);

		if (ascending) {
			qb.ascending(orderBy);
		} else {
			qb.descending(orderBy);
		}

		return connector.searchUsers(qb.build(), sessionData.getAccesstoken());
	}

	/**
	 * Returns the user with the given ID.
	 *
	 * @param id
	 *        the user ID
	 * @return the requested user
	 * @throws NoSuchUserException
	 *         if the no user was found for id.
	 */
	public User getUser(String id) {
		return connector.getUser(id, sessionData.getAccesstoken());
	}

	/**
	 * Revoke the current access token
	 */
	public void logoutCurrentUser() {
		connector.revokeAccessToken(sessionData.getAccesstoken());
	}

	/**
	 * Create the given.
	 *
	 * @param createUser
	 * 	the user
	 *
	 * @return User
	 * 	the created user
	 */
	public User createUser(User createUser) {
		return connector.createUser(createUser, sessionData.getAccesstoken());
	}

	/**
	 * Updates a user based on the given {@link UpdateUser}.
	 *
	 * @param id
	 *        the user ID
	 * @param updateUser
	 *        the {@link UpdateUser}
	 */
	public void updateUser(String id, UpdateUser updateUser) {
		connector.updateUser(id, updateUser, sessionData.getAccesstoken());
	}

	/**
	 * Deactivate the user by the given userId.
	 *
	 * @param id
	 *        the user ID
	 */
	public void deactivateUser(String id) {
		UpdateUser updateUser = new UpdateUser.Builder().updateActive(false).build();

		connector.updateUser(id, updateUser, sessionData.getAccesstoken());
	}

	/**
	 * Activate the user by the given userId.
	 *
	 * @param id
	 *        the user ID
	 */
	public void activateUser(String id) {
		UpdateUser updateUser = new UpdateUser.Builder().updateActive(true).build();

		connector.updateUser(id, updateUser, sessionData.getAccesstoken());
	}

	/**
	 * Delete the user by the given userId.
	 *
	 * @param id
	 *        the user ID
	 */
	public void deleteUser(String id) {
		connector.deleteUser(id, sessionData.getAccesstoken());
	}

	/**
	 * Replace a user based on the given {@link User} and revoke his access token.
	 *
	 * @param id
	 *        the user ID
	 * @param User
	 *        the {@link User}
	 */
	public void replaceUser(String id, User newUser) {
		connector.replaceUser(id, newUser, sessionData.getAccesstoken());
		connector.revokeAllAccessTokens(id, sessionData.getAccesstoken());
	}

	/**
	 * Return all the other users which are a member of the given group.
	 *
	 * @param groupId
	 *            The group id.
	 * @param attributes
	 *        List of attributes to return.
	 * @return A SCIMSearchResult containing a list of all found users.
	 */
	public SCIMSearchResult<User> getAssignedUsers(String groupId, PagingInformation pagingInformation, String attributes) {
		String buildedQuery = "groups eq \"" + groupId + "\"";

		if(pagingInformation.getQuery() != null && !pagingInformation.getQuery().trim().isEmpty()) {
			buildedQuery += " and " + pagingInformation.getQuery();
		}

		return searchUser(buildedQuery,
				pagingInformation.getLimit(),
				pagingInformation.getOffset(),
				pagingInformation.getOrderBy(),
				pagingInformation.getAscending(),
				attributes);
	}

	/**
	 * Return all the other users which are not a member of the given group.
	 *
	 * @param groupId
	 *            The group id.
	 * @param attributes
	 *        List of attributes to return.
	 * @return A SCIMSearchResult containing a list of all found users.
	 */
	public SCIMSearchResult<User> getUnassignedUsers(String groupId, PagingInformation pagingInformation, String attributes) {
			String buildedQuery = "not(groups eq \"" + groupId + "\") or not(groups pr)";

			if(pagingInformation.getQuery() != null && !pagingInformation.getQuery().trim().isEmpty()) {
				buildedQuery += " and " + pagingInformation.getQuery();
			}

			return searchUser(buildedQuery,
					pagingInformation.getLimit(),
					pagingInformation.getOffset(),
					pagingInformation.getOrderBy(),
					pagingInformation.getAscending(),
					attributes);
		}
}

package com.gDyejeekis.aliencompanion.api.retrieval;

import static com.gDyejeekis.aliencompanion.utils.JsonUtils.safeJsonToString;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.gDyejeekis.aliencompanion.models.RedditItem;
import com.gDyejeekis.aliencompanion.api.entity.Comment;
import com.gDyejeekis.aliencompanion.api.entity.Kind;
import com.gDyejeekis.aliencompanion.models.MoreComment;
import com.gDyejeekis.aliencompanion.api.entity.Submission;
import com.gDyejeekis.aliencompanion.api.entity.User;
import com.gDyejeekis.aliencompanion.api.exception.RetrievalFailedException;
import com.gDyejeekis.aliencompanion.api.exception.RedditError;
import com.gDyejeekis.aliencompanion.api.retrieval.params.CommentSort;
import com.gDyejeekis.aliencompanion.api.retrieval.params.TimeSpan;
import com.gDyejeekis.aliencompanion.api.retrieval.params.UserOverviewSort;
import com.gDyejeekis.aliencompanion.api.utils.ApiEndpointUtils;
import com.gDyejeekis.aliencompanion.api.utils.ParamFormatter;
import com.gDyejeekis.aliencompanion.api.utils.httpClient.HttpClient;

import okhttp3.FormBody;
import okhttp3.RequestBody;

/**
 * This class offers the following functionality:
 * 1) Parsing the results of a request into Comment objects (see <code>Comments.parseBreadth()</code> and <code>Comments.parseDepth()</code>).
 * 2) The ability to get comments of a user (see <code>Commments.ofUser()</code>).
 * 3) The ability to get comments of a submission/article (see <code>Comments.ofSubmission()</code>).
 * 
 * @author Raul Rene Lepsa
 * @author Simon Kassing
 */
public class Comments implements ActorDriven {

    private HttpClient httpClient;
    private User user;

	private boolean syncRetrieval = false;

	public void setSyncRetrieval(boolean flag) {
		syncRetrieval = flag;
	}

    /**
     * Constructor. Global default user (null) is used.
     * @param httpClient REST Client instance
     */
    public Comments(HttpClient httpClient) {
        this.httpClient = httpClient;
        this.user = null;
    }
    
    /**
     * Constructor.
     * @param httpClient REST Client instance
     * @param actor User instance
     */
    public Comments(HttpClient httpClient, User actor) {
    	this.httpClient = httpClient;
        this.user = actor;
    }
    
    /**
     * Switch the current user for the new user who will
     * be used when invoking retrieval requests.
     * 
     * @param new_actor New user
     */
    public void switchActor(User new_actor) {
    	this.user = new_actor;
    }
    
    /**
     * Parses a JSON feed of comments from Reddit (URL) into a nice list of Comment objects
     * maintaining the order. This parses ONLY the first depth of comments. Only call
     * this function to parse shallow comment listings (e.g. of the user overview).
     * 
     * @param url		URL for the request
     * 
     * @return Parsed list of comments.
     */
    public List<RedditItem> parseBreadth(String url) throws RetrievalFailedException, RedditError {
    	
    	// Determine cookie
    	String cookie = (user == null) ? null : user.getCookie();
    	
    	// List of submissions
        List<RedditItem> comments = new LinkedList<>();
        
        // Send request to reddit server via REST client
        Object response = httpClient.get(ApiEndpointUtils.REDDIT_CURRENT_BASE_URL, url, cookie).getResponseObject();

        if (response instanceof JSONObject) {
        	
	        JSONObject object = (JSONObject) response;
	        if (object.get("error") != null) {
	        	throw new RedditError("Comments response contained error code " + object.get("error") + ".");
	        }
	        
	        JSONArray array = (JSONArray) ((JSONObject) object.get("data")).get("children");
	        
	        // Iterate over the submission results
	        JSONObject data;
	        Comment comment;
	        for (Object anArray : array) {
	            data = (JSONObject) anArray;
	            
	            // Make sure it is of the correct kind
	            String kind = safeJsonToString(data.get("kind"));
				if (kind != null) {
					if (kind.equals(Kind.COMMENT.value())) {
						// Contents of the comment
						data = ((JSONObject) data.get("data"));
						//create and add the new comment
                        comment = new Comment(data);
						comments.add(comment);
                    }
				}
			}
        
        } else {
        	throw new IllegalArgumentException("Parsing failed because JSON is not from a shallow comment listing.");
        }

        // Finally return list of submissions 
        return comments;
        
    }

    /**
     * Parses a JSON feed of comments from Reddit (URL) into a nice list of Comment objects
     * maintaining the order. This parses all comments that are defined with their associated values,
     * those that fall outside the (default) limit are omitted.
     * 
     * @param url		URL for the request
     * 
     * @return Parsed list of comments.
     */
    public List<Comment> parseDepth(String url) throws RetrievalFailedException, RedditError {
    	
    	// Determine cookie
    	String cookie = (user == null) ? null : user.getCookie();
    	
    	// List of submissions
        List<Comment> comments = new LinkedList<>();
        
        // Send request to reddit server via REST client
        Object response = httpClient.get(ApiEndpointUtils.REDDIT_CURRENT_BASE_URL, url, cookie).getResponseObject();
        
        if (response instanceof JSONArray) {
        	
        	JSONObject object =  (JSONObject) ((JSONArray) response).get(1);
        	parseRecursive(comments, object);
	        
        } else {
        	throw new IllegalArgumentException("Parsing failed because JSON input is not from a submission.");
        }
        
        return comments;
	        
    }

	public List<Comment> parseDepth(String url, Submission post) throws RetrievalFailedException, RedditError {

		// Determine cookie
		String cookie = (user == null) ? null : user.getCookie();

		// List of submissions
		List<Comment> comments = new LinkedList<>();

		// Send request to reddit server via REST client
		Object response = httpClient.get(ApiEndpointUtils.REDDIT_CURRENT_BASE_URL, url, cookie).getResponseObject();

		if (response instanceof JSONArray) {

			//Retrieve post
			JSONObject postObject = (JSONObject) ((JSONArray) response).get(0);
			JSONArray children = (JSONArray) ((JSONObject) postObject.get("data")).get("children");
			JSONObject child = (JSONObject) ((JSONArray) children).get(0);
			JSONObject postData = (JSONObject) child.get("data");
			post.updateSubmission(postData);

			//Retrieve comments
			JSONObject commentsObject =  (JSONObject) ((JSONArray) response).get(1);
			parseRecursive(comments, commentsObject);

		} else {
			throw new IllegalArgumentException("Parsing failed because JSON input is not from a submission.");
		}

		return comments;

	}
    
    /**
     * Parse a JSON object consisting of comments and add them
     * to the already existing list of comments. This does NOT create
     * a new comment list.
     * 
     * @param comments 	List of comments
     * @param object	JSON Object
     */
    protected void parseRecursive(List<Comment> comments, JSONObject object) throws RetrievalFailedException, RedditError {
    	assert comments != null : "List of comments must be instantiated.";
    	assert object != null : "JSON Object must be instantiated.";
    	
    	// Get the comments in an array
        JSONArray array = (JSONArray) ((JSONObject) object.get("data")).get("children");
        
        // Iterate over the submission results
        JSONObject data;
		Comment comment;
        for (Object anArray : array) {
            data = (JSONObject) anArray;
            
            // Make sure it is of the correct kind
            String kind = safeJsonToString(data.get("kind"));
			if (kind != null) {
				if (kind.equals(Kind.COMMENT.value())) {

                    // Contents of the comment
                    data = ((JSONObject) data.get("data"));

                    // Create and add the new comment
                    comment = new Comment(data);
					comments.add(comment);

                    Object o = data.get("replies");
                    if (o instanceof JSONObject) {

                        // Dig towards the replies
                        JSONObject replies = (JSONObject) o;
                        parseRecursive(comment.getReplies(), replies);
                    }

                }
				else if (!syncRetrieval && kind.equals(Kind.MORE.value())) {

					data = (JSONObject) data.get("data");
					JSONArray jsonArray = (JSONArray) data.get("children");
					if (jsonArray!=null && jsonArray.size()!=0) {
						comment = new MoreComment(data, jsonArray);
						comments.add(comment);
					}
                }
			}

		}
        
    }
    
    /**
     * Get the comment tree of the given user.
     * In this variant all parameters are Strings.
     *
     * @param username	 		Username of the user you want to retrieve from.
     * @param sort	    		(Optional, set null if not used) Sorting method.
     * @param time		 		(Optional, set null is not used) Time window
     * @param count        		(Optional, set null if not used) Number at which the counter starts
     * @param limit        		(Optional, set null if not used) Integer representing the maximum number of comments to return
     * @param after				(Optional, set null if not used) After which comment to retrieve
     * @param before			(Optional, set null if not used) Before which comment to retrieve
     * @param show				(Optional, set null if not used) Show parameter ('given' is only acceptable value)
     * 
     * @return Comments of a user.
     */
    public List<RedditItem> ofUser(String username, String sort, String time, String count, String limit, String after, String before, String show) throws RetrievalFailedException, RedditError {
        
    	// Format parameters
    	String params = "";
    	params = ParamFormatter.addParameter(params, "sort", sort);
    	params = ParamFormatter.addParameter(params, "time", time);
    	params = ParamFormatter.addParameter(params, "count", count);
    	params = ParamFormatter.addParameter(params, "limit", limit);
    	params = ParamFormatter.addParameter(params, "after", after);
    	params = ParamFormatter.addParameter(params, "before", before);
    	params = ParamFormatter.addParameter(params, "show", show);
    	
        // Retrieve submissions from the given URL
        return parseBreadth(String.format(ApiEndpointUtils.USER_COMMENTS, username, params));
        
    }
    
    /**
     * Get the comment tree of the given user (username).
     *
     * @param username	 		Username of the user you want to retrieve from.
     * @param sort	    		(Optional, set null if not used) Sorting method.
     * @param time		 		(Optional, set null is not used) Time window
     * @param count        		(Optional, set -1 if not used) Number at which the counter starts
     * @param limit        		(Optional, set -1 if not used) Integer representing the maximum number of comments to return
     * @param after				(Optional, set null if not used) After which comment to retrieve
     * @param before			(Optional, set null if not used) Before which comment to retrieve
     * @param show_given		(Optional, set false if not used) Only show the given comments
     * 
     * @return Comments of a user.
     */
    public List<RedditItem> ofUser(String username, UserOverviewSort sort, TimeSpan time, int count, int limit, Comment after, Comment before, boolean show_given) throws RetrievalFailedException, RedditError {
       
    	if (username == null || username.isEmpty()) {
    		throw new IllegalArgumentException("The username must be set.");
    	}
        
    	//return ofUser(
		//		username,
		//		(sort != null) ? sort.value() : null,
		//		(time != null) ? time.value() : null,
		//		String.valueOf(count),
		//		String.valueOf(limit),
		//		(after != null) ? after.getFullName() : null,
		//		(before != null) ? before.getFullName() : null,
		//		(show_given) ? "given" : null
		//);
		List<RedditItem> comments = new ArrayList<>();
		comments.addAll(ofUser(
				username,
				(sort != null) ? sort.value() : null,
				(time != null) ? time.value() : null,
				String.valueOf(count),
				String.valueOf(limit),
				(after != null) ? after.getFullName() : null,
				(before != null) ? before.getFullName() : null,
				(show_given) ? "given" : null
		));

		return comments;
    }
    
    /**
     * Get the comment tree of the given user (object).
     *
     * @param target	 		User you want to retrieve from.
     * @param sort	    		(Optional, set null if not used) Sorting method.
     * @param time		 		(Optional, set null is not used) Time window
     * @param count        		(Optional, set -1 if not used) Number at which the counter starts
     * @param limit        		(Optional, set -1 if not used) Integer representing the maximum number of comments to return
     * @param after				(Optional, set null if not used) After which comment to retrieve
     * @param before			(Optional, set null if not used) Before which comment to retrieve
     * @param show_given		(Optional, set false if not used) Only show the given comments
     * 
     * @return Comments of a user.
     */
    public List<RedditItem> ofUser(User target, UserOverviewSort sort, TimeSpan time, int count, int limit, Comment after, Comment before, boolean show_given) throws RetrievalFailedException, RedditError {
    	
    	if (target == null) {
    		throw new IllegalArgumentException("The user targeted must be set.");
    	}
    	
    	return ofUser(target.getUsername(), sort, time, count, limit, after, before, show_given);
    	
    }

    /**
     * Get the comment tree from a given submission.
     * In this variant all parameters are Strings.
     *
     * @param submission 		Submission ID36 identifier
     * @param commentId    		(Optional, set null if not used) ID of a comment. If specified, this comment will be the focal point of the returned view.
     * @param parentsShown 		(Optional, set -1 is not used) An integer between 0 and 8 representing the number of parents shown for the comment identified by <code>commentId</code>
     * @param depth        		(Optional, set -1 if not used) Integer representing the maximum depth of subtrees in the thread
     * @param limit        		(Optional, set -1 if not used) Integer representing the maximum number of comments to return
     * @param sort  			(Optional, set null if not used) CommentSort enum indicating the type of sorting to be applied (e.g. HOT, NEW, TOP, etc)
     * @return Comments for an article.
     */
    public List<Comment> ofSubmission(Submission submission, String commentId, int parentsShown, int depth, int limit, CommentSort sort) {

		if (submission == null) {
			throw new IllegalArgumentException("The submission must be defined.");
		}

    	// Format parameters
    	String params = "";
    	params = ParamFormatter.addParameter(params, "comment", commentId);
    	params = ParamFormatter.addParameter(params, "context", Integer.toString(parentsShown));
    	params = ParamFormatter.addParameter(params, "depth", Integer.toString(depth));
    	params = ParamFormatter.addParameter(params, "limit", Integer.toString(limit));
    	params = ParamFormatter.addParameter(params, "sort", sort.value());
    	
        // Retrieve submissions from the given URL
        return parseDepth(String.format(ApiEndpointUtils.SUBMISSION_COMMENTS, submission.getIdentifier(), params), submission);
        
    }

	public List<Comment> moreChildren(String linkId, List<String> children, CommentSort sort) {
		List<Comment> comments = new LinkedList<>();

		// Determine cookie
		String cookie = (user == null) ? null : user.getCookie();

		String replies = "";
		for(String id : children) {
			replies = replies.concat(id);
			if(children.indexOf(id) != children.size()-1) {
				replies = replies.concat(",");
			}
		}

		RequestBody body = new FormBody.Builder().add("api_type", "json").add("children", replies).add("link_id", linkId).add("sort", sort.value()).build();
		JSONObject object = (JSONObject) httpClient.post(ApiEndpointUtils.REDDIT_CURRENT_BASE_URL, body, ApiEndpointUtils.SUBMISSION_MORE_COMMENTS, cookie).getResponseObject();

		JSONObject json = (JSONObject) object.get("json");
		JSONObject data = (JSONObject) json.get("data");
		JSONArray things = (JSONArray) data.get("things");
		for(Object o : things) {
			JSONObject jsonObject = (JSONObject) o;
			String kind = (String) jsonObject.get("kind");
			if(kind!=null) {
				JSONObject commentData = (JSONObject) jsonObject.get("data");
				if(kind.equals(Kind.COMMENT.value())) {
					comments.add(new Comment(commentData));
				}
				else if(kind.equals(Kind.MORE.value())) {
					JSONArray jsonArray = (JSONArray) commentData.get("children");
					if(jsonArray!=null && jsonArray.size()!=0) {
						comments.add(new MoreComment(commentData, jsonArray));
					}
				}
			}
		}

		return comments;
	}

    ///**
    // * Get the comment tree from a given submission (object).
    // *
    // * @param submission 		Submission object
    // * @param commentId    		(Optional, set null if not used) ID of a comment. If specified, this comment will be the focal point of the returned view.
    // * @param parentsShown 		(Optional, set -1 is not used) An integer between 0 and 8 representing the number of parents shown for the comment identified by <code>commentId</code>
    // * @param depth        		(Optional, set -1 if not used) Integer representing the maximum depth of subtrees in the thread
    // * @param limit        		(Optional, set -1 if not used) Integer representing the maximum number of comments to return
    // * @param sort  			(Optional, set null if not used) CommentSort enum indicating the type of sorting to be applied (e.g. HOT, NEW, TOP, etc)
    // * @return Comments for an article.
    // */
    //public List<Comment> ofSubmission(Submission submission, String commentId, int parentsShown, int depth, int limit, CommentSort sort) throws RetrievalFailedException, RedditError {
    //
    //	if (submission == null) {
    //		throw new IllegalArgumentException("The submission must be defined.");
    //	}
    //
    //	return ofSubmission(submission, commentId, parentsShown, depth, limit, sort);
    //}
    
	///**
	// * Flatten the comment tree.
	// * The original comment tree is not overwritten.
	// *
	// * @param cs		List of comments that you get returned from one of the other methods here
	// * @param target	List in which to place the flattend comment tree.
	// */
	//public static void flattenCommentTree(List<Comment> cs, List<Comment> target) {
	//	for (Comment c : cs) {
	//		target.add(c);
	//		flattenCommentTree(c.getReplies(), target);
	//	}
	//}

	public static void indentCommentTree(List<Comment> comments) {
		for(Comment c : comments) {
			List<Comment> replies = c.getReplies();
			if(replies!=null && replies.size() != 0) {
				for(Comment r : replies) {
					r.setIndentation(c.getIndentation()+1);
					indentCommentTree(replies);
				}
			}
		}
	}
	
	/**
	 * Print the given comment tree.
	 * @param cs 	List of comments that you get returned from one of the other methods here
	 */
	public static void printCommentTree(List<Comment> cs) {
		for (Comment c : cs) {
			printCommentTree(c, 0);
		}
	}
	
	/**
	 * Print the comment at a specific level. Recursive function.
	 * @param c			Comment
	 * @param level		Level to place at
	 */
	private static void printCommentTree(Comment c, int level) {
		for (int i = 0; i < level; i++) {
			System.out.print("\t");
		}
		System.out.println(c);
		for (Comment child : c.getReplies()) {
			printCommentTree(child, level + 1);
		}
	}
    
}

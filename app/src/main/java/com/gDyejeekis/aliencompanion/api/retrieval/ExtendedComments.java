package com.gDyejeekis.aliencompanion.api.retrieval;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.gDyejeekis.aliencompanion.models.RedditItem;
import com.gDyejeekis.aliencompanion.api.entity.Comment;
import com.gDyejeekis.aliencompanion.api.entity.Submission;
import com.gDyejeekis.aliencompanion.api.retrieval.params.CommentSort;
import com.gDyejeekis.aliencompanion.api.retrieval.params.TimeSpan;
import com.gDyejeekis.aliencompanion.api.retrieval.params.UserOverviewSort;
import com.gDyejeekis.aliencompanion.api.utils.RedditConstants;

public class ExtendedComments {

	private Comments comments;

	public ExtendedComments(Comments comments) {
		this.comments = comments;
	}

    /**
     * Get the comment list from a given submission (ID36).
     * The result is maximum amount_first_depth * RedditConstants.MAX_LIMIT_COMMENTS
     * Which can be quite alot (e.g. 500x500 = 250000 comments)
     *
     * @param submission 			Submission object
     * @param sort  				(Optional, set null if not used) CommentSort enum indicating the type of sorting to be applied (e.g. HOT, NEW, TOP, etc)
     * @param amount_first_depth	(Optional, set -1 if not used) Integer representing the amount of first depth comments to retrieve
     * @param after					(Optional, set to null if not used) After which comment to retrieve
     * @return Comments for an article.
     */
    public List<Comment> ofSubmission(Submission submission, CommentSort sort, int amount_first_depth, Comment after) {
    	
    	if (amount_first_depth < -1) {
    		throw new IllegalArgumentException("A negative amount of comments is not allowed.");
    	}
    	
    	int limit = amount_first_depth > RedditConstants.MAX_LIMIT_COMMENTS ? RedditConstants.MAX_LIMIT_COMMENTS : amount_first_depth;
    	
    	// List of first depth comments
        List<Comment> result = comments.ofSubmission(submission, null, -1, 1, limit, sort);

        // Retrieval the deeper comments for each first depth comment
        for (Comment c : result) {

        	// If there is one reply at least
        	if (c.hasRepliesSomewhere()) {

        		// Deeper comments, more than 500 if extremely rare.
        		List<Comment> subresult = comments.ofSubmission(submission, c.getIdentifier(), -1, 8, RedditConstants.MAX_LIMIT_COMMENTS, sort);
        		
        		//System.out.println(subresult.size());
        		c.setReplies(subresult.get(0).getReplies());
        	 	
        	}

        }

		return result;
    	
    }

    /**
     * Get the comment list of the given user (username).
     *
     * @param username	 		Username of the user you want to retrieve from.
     * @param sort	    		(Optional, set null if not used) Sorting method.
     * @param time		 		(Optional, set null is not used) Time window
     * @param amount        	(Optional, set -1 if not used) Integer representing the desired amount of comments to return
     * @param after				(Optional, set null if not used) After which comment to retrieve
     * 
     * @return Comments of a user.
     */
    public List<RedditItem> ofUser(String username, UserOverviewSort sort, TimeSpan time, int amount, Comment after) {
    	
    	if (amount < 0) {
    		throw new IllegalArgumentException("A negative amount of comments is not allowed.");
    	}

    	// List of comments
        List<RedditItem> result = new LinkedList<>();

        // Do all iterations
        int counter = 0;
		while (amount >= 0) {
			
			// Determine how much still to retrieve in this iteration
			int limit = (amount < RedditConstants.MAX_LIMIT_LISTING) ? amount : RedditConstants.MAX_LIMIT_LISTING;
			amount -= limit;
			
			// Retrieve comments
			List<RedditItem> subresult = comments.ofUser(username, sort, time, counter, limit, after, null, true);
			if (subresult == null) {
				return new ArrayList<>();
			}
			result.addAll(subresult);
			
			// Increment counter
			counter += limit;
			
			// If the end of the comment stream has been reached
			if (subresult.size() != limit) {
				//System.out.println("API Stream finished prematurely: received " + subresult.size() + " but wanted " + limit + ".");
				break;
			}
			
			// If nothing is left desired, exit.
			if (amount <= 0) {
				break;
			}
			
			// Previous last comment
			after = (Comment) subresult.get(subresult.size() - 1);
			
		}
		
		return result;
    	
    }
    
}

package com.luminia.tradegems.database;

import java.util.Comparator;

public class Score implements Comparator<Score>{
	
	protected String accountname;
	protected Long score;
	protected Integer turn;
	
	public Score(){}
	
	public Score(Long s){
		score = s;
	}
	
	public Score(long s){
		score = s;
	}

	public Long getScore() {
		return score;
	}

	public void setScore(Long score) {
		this.score = score;
	}
	
	public String getAccountName() {
		return accountname;
	}

	public void setAccountName(String aname) {
		this.accountname = aname;
	}

	@Override
	public int compare(Score lhs, Score rhs) {
		if(lhs.getScore() < rhs.getScore())
			return -1;
		else if (lhs.getScore() > rhs.getScore())
			return 1;
		return 0;
	}
}

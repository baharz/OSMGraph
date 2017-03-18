package project;

public class Time {
	// member variables
	private int hour=0;
	private int minute=0;
	// get functions
	public int hour()
	{
		return this.hour;
	}
	public int minute()
	{
		return this.minute;
	}
	
	//set functions
	public void setHour(int hour)
	{
		if(hour<0 || hour > 24)
			System.out.println("The value for hour is invalid, used defualt value to initialize!");
		else
			this.hour = hour;
	}
	public void setMinute(int min)
	{
		if(min<0 || min>60)
			System.out.println("The value for minute is invalid, used defualt value to initialize!");
		else
			this.minute= min; 
	}
	
	//constructors
	public Time(){}
	public Time(int hour,int min)
	{
		if(hour<0 || hour > 24)
			System.out.println("The value for hour is invalid, used defualt value to initialize!");
		else
			this.hour = hour;
		
		if(min<0 || min>60)
			System.out.println("The value for minute is invalid, used defualt value to initialize!");
		else
			this.minute= min; 
		
	}
	
	//methods
	public int convertToMinutes()
	{
		return this.hour*60 + this.minute;
	}
	

}

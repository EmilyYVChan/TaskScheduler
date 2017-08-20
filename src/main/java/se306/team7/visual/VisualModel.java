package se306.team7.visual;

import javafx.animation.Timeline;

import java.util.List;

import javafx.animation.KeyFrame;
import javafx.util.Duration;
import se306.team7.Metrics;
import javafx.event.EventHandler;
import javafx.event.ActionEvent;


public class VisualModel {
	
	 private List<ITaskSchedulerView> _views;
	 private Timeline _updatePerMinute;
	 
	 public VisualModel(){
		 _views.add(View_CurrentBest.getInstance());
		 _views.add(View_Histogram.getInstance());
		 _views.add(View_LineGraph.getInstance()); 
		 
		 setUpTimer();
	 }
	 
	private void setUpTimer(){
		_updatePerMinute = new Timeline(new KeyFrame(Duration.seconds(1), new EventHandler<ActionEvent>(){

			    @Override
			    public void handle(ActionEvent event) {
			        System.out.println("this is called every second to update graphs");
			        
			        for(ITaskSchedulerView view : _views){
			        	view.update(Metrics.getCurrentBestCost(), Metrics.getHistogram(), Metrics.getCoreCurrentLevel());
			        }
			        
			    }
			}));
		_updatePerMinute.setCycleCount(Timeline.INDEFINITE);
	 }
	
	public void startTimer(){

		_updatePerMinute.play();
	}
}

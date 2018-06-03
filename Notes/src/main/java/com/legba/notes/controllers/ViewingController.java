package com.legba.notes.controllers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.legba.notes.elements.Presentation;
import com.legba.notes.elements.Slide;
import com.legba.notes.elements.base.SlideElement;
import com.legba.notes.models.AppModel;
import com.legba.notes.nodes.PdfView;
import com.legba.notes.renderers.PresentationRenderer;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.text.Text;
import javafx.util.Duration;

/**
 * Controller for the viewing screen containing notes and PDF viewer
 * @author vc622 and lm1370 and hjew501
 *
 */
public class ViewingController {
	
	public List<MediaPlayer> allMediaPlayers = new ArrayList<>();

	
	
	private double nodeX;
	private double nodeY;
	private SlideElement nodeElement;
	public String pdfURL;
		
	@FXML
	private SplitPane viewing_root;
	
	@FXML
	private VBox reference_root;
	
	@FXML
	private VBox notes_root;
	
	@FXML 
	public Text actiontarget;
	
	public Double getReferenceRootTime() {
		
		Duration duration = null;
		Double durationInt;
		for(Node nodeIn:reference_root.getChildren()){
            if(nodeIn instanceof MovieView){
                duration = ((MovieView)nodeIn).getMediaPlayer().getCurrentTime();
            }
        }
		durationInt = duration.toSeconds();
		return durationInt;
	}
	
	/**
	 * Returns slide size index
	 * @return
	 */
	public double[] slideSize() {
		double[] slideSizeIndex;
		
		VBox slideBox = ((VBox)((ScrollPane)notes_root.getChildren().get(0)).getContent());
		
		slideSizeIndex = new double[slideBox.getChildren().size()];
		
		for(int i = 0; i<slideBox.getChildren().size(); i++){
			if (slideBox.getChildren().get(i) instanceof Text){
				Text Slide = ((Text)slideBox.getChildren().get(i));
				slideSizeIndex[i] = Slide.getLayoutBounds().getHeight();
			}
			if (slideBox.getChildren().get(i) instanceof Pane){
				Pane Slide = ((Pane)slideBox.getChildren().get(i));
				slideSizeIndex[i] = Slide.getLayoutBounds().getHeight();
			}
			slideSizeIndex[i] += 10;
			//System.out.println("Slide Height is: " + Slide.getLayoutBounds().getHeight());
		}
		return slideSizeIndex;
		
	}
	
	/**
	 * Enables scrolling on slides
	 * @param slideIndex
	 */
	public void scrollToSlide(int slideIndex){
		
		double[] slideLengths = slideSize();
		
		if (slideIndex > slideLengths.length -1){
			slideIndex = slideLengths.length -1;
		}

		//slideIndex = 0;
		
		VBox slideBox = ((VBox)((ScrollPane)notes_root.getChildren().get(0)).getContent());
		ScrollPane scrollPane = (ScrollPane)notes_root.getChildren().get(0);
		double totalSlideSize = slideBox.getHeight();
		
		double nextSlideHeight = slideLengths[0];
		for(int i = 1; i <= slideIndex+1; i++){
			nextSlideHeight+=slideLengths[i];
		}
		
		System.out.println("slideIndex: " + slideIndex);
		System.out.println("slideLengths: " + Arrays.toString(slideLengths));
		System.out.println("totalSlideSize: " + totalSlideSize);
		System.out.println("nextSlideHeight: " + nextSlideHeight);
		System.out.println("actual scroll size: " + nextSlideHeight/totalSlideSize);
		System.out.println("actual scroll size: " + slideLengths.length);
		System.out.println("vvalue: " + ((ScrollPane)notes_root.getChildren().get(0)).getVvalue());
		
		Double h = slideBox.getBoundsInLocal().getHeight();
		Double v = scrollPane.getViewportBounds().getHeight();
		
		Double value = scrollPane.getVmax() * ((nextSlideHeight)/(h - v));
		
		System.out.println(value);
		
		((ScrollPane)notes_root.getChildren().get(0)).setVvalue(value);
	}
	
	/**
	 * Updates all slides when changes are made from the toolbar controller
	 */
	public void updateSlide() {
		
		//Storage for playback locations and status
		ArrayList<Duration> currentPlayback = new ArrayList<Duration>();
		ArrayList<MediaPlayer.Status> currentStatus = new ArrayList<MediaPlayer.Status>();
			
		//Get current scroll location
		double currentScroll = ((ScrollPane)notes_root.getChildren().get(0)).getVvalue();
		
		// Get current playback locations and current status for all media
		for(MediaPlayer m : this.allMediaPlayers) {
			currentPlayback.add(m.getCurrentTime());
			currentStatus.add(m.getStatus());
		}
			
		// Stop all current playing media and remove media player storage
		stopAllMedia();
		this.allMediaPlayers.clear();
		
		// Get the presentation from the model
		Presentation pres = AppController.getInstance().toolbar.CurrentPres;
		
		// Re-render the presentation
		PresentationRenderer pr = new PresentationRenderer();
		
		// Display the presentation
		notes_root.getChildren().clear();
		notes_root.getChildren().add(pr.render(pres));
		
		//Add variable data for new media player
		if(AppController.getInstance().toolbar.AddElement == true) {
			currentPlayback.add(Duration.ZERO);
			currentStatus.add(MediaPlayer.Status.STOPPED);
		}
		
		// Set playback locations and status for all media
		for(MediaPlayer m : this.allMediaPlayers) {
			
			//If media player was playing, continue playing otherwise stop
			if (currentStatus.get(allMediaPlayers.indexOf(m)) == MediaPlayer.Status.PLAYING)
                m.play();
            else 
                m.stop();
			
			//Set playback position
			m.setStartTime(currentPlayback.get(allMediaPlayers.indexOf(m)));
		}
		
		// Set scroll to previous position
		((ScrollPane)notes_root.getChildren().get(0)).setVvalue(currentScroll);	
	}
	
	/**
	 * Sets all media currently playing to "stop"
	 */
	public void stopAllMedia() {
		//For all mediaPlayers rendered, apply stop method
		for(MediaPlayer m : this.allMediaPlayers) {
			m.stop();
		}
	}
	
	
	public void scrollToDuration(Duration duration) {
		
		List<Slide> slides = AppModel.getInstance().getPres().getSlide();
		List<Integer> slidesDuration = new ArrayList<Integer>();
		List<Integer> slidesIndex = new ArrayList<Integer>();
		for (int i = 0; i < slides.size(); i++) {
			if (slides.get(i).getDuration() != null) {
				slidesDuration.add(slides.get(i).getDuration());
				slidesIndex.add(i);
			}
		};
		
		int temp = 0;
		if (slidesDuration != null) {
				for (int i = 0; i < slidesDuration.size(); i++) {
					for(int j = 1; j < (slidesDuration.size() - i); j++) {
						if (slidesDuration.get(j - 1) > slidesDuration.get(j)) {
							temp = slidesDuration.get(j - 1);
							slidesDuration.set(j - 1, slidesDuration.get(j));
							slidesDuration.set(j, temp);
							temp = slidesIndex.get(j - 1);
							slidesIndex.set(j - 1, slidesIndex.get(j));
							slidesIndex.set(j, temp);
						}
					}
				}
		}		
		
		int slideToMoveTo = -1;
		
		for (int i= 0; i < slidesDuration.size(); i++) {
			if (duration.toSeconds() > slidesDuration.get(i)) {
				slideToMoveTo = slidesIndex.get(i);
			}
		}
		if (slideToMoveTo == -1) {
			slideToMoveTo = 0;
		}
		
		scrollToSlide(slideToMoveTo);
		
		System.out.println("Duration: " + slidesDuration + ". Slides: " + slidesIndex);
	}
	
	/**
	 * Method that controls moving elements. If element hasn't been 
	 * moved it returns false. Then gets the combination of the 
	 * elements pre-existing coordinates and the node offset and checks
	 * that neither the x or y components are negative. It then sets the
	 * coordinates of the element to these new values.
	 * 
	 * @param s
	 * @param n
	 * @return boolean
	 */
	public boolean moveElement(SlideElement s, Node n) {		
		//If element hasn't moved, exit out
		if((n.getTranslateX() == 0) && (n.getTranslateY() == 0)) {
			return false;
		}
		
		//Get current coordinates
		nodeX = s.getX() + n.getTranslateX();
		nodeY = s.getY() + n.getTranslateY();
		
		//Check that x is not negative
		if(nodeX < 0) {
			nodeX = 0;
		}
		
		//Check that y is not negative
		if(nodeY < 0) {
			nodeY = 0;
		}
		
		//Set coordinates
		s.setX2((float) (nodeX + s.getWidth()));
		s.setX((float) nodeX);
		s.setY2((float) (nodeY + s.getHeight()));
		s.setY((float) nodeY);	

		return true;
	}
	
	/**
	 * Constructor
	 */
	public ViewingController(){
		
	}

	/**
	 * Initialise method
	 */
 	@FXML
    void initialize(){
		// Called once all variable with @FXML have been populated
 		AppController.getInstance().viewing = this;

 		// get the presentation from the model
		Presentation pres = AppModel.getInstance().getPres();
		
		// render the presentation
		PresentationRenderer pr = new PresentationRenderer();

		// display the presentation
		notes_root.getChildren().clear();
		notes_root.getChildren().add(pr.render(pres));
		
		String pdfURL = "https://courses.physics.illinois.edu/phys580/fa2013/uncertainty.pdf";
		
		PdfView pdfView = new PdfView(pdfURL);
		//http://www.iupui.edu/~womrel/REL%20300%20Spirit/REL%20300_Spirit/Lwa.pdf
		//https://courses.physics.illinois.edu/phys580/fa2013/uncertainty.pdf
		//http://www.metaphysicspirit.com/books/The%20Voodoo%20Hoodoo%20Spellbook.pdf
		
		//MovieView movieView = new MovieView ("local_file.mp4",40,65,600,500);
		reference_root.getChildren().clear();
		reference_root.getChildren().add(pdfView);
				
	}
 	
 	public void refresh(String pdfURL){
 		PdfView pdfView = new PdfView(pdfURL);
 		reference_root.getChildren().clear();
		reference_root.getChildren().add(pdfView); 		
 	};

// 	public void incrementSlide(){
// 		curSlideIndex++;
// 	}
// 	public void decrementSlide(){
// 		curSlideIndex--;
// 	}
}

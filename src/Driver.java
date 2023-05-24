import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.openstreetmap.gui.jmapviewer.Coordinate;
import org.openstreetmap.gui.jmapviewer.JMapViewer;
import org.openstreetmap.gui.jmapviewer.MapPolygonImpl;
import org.openstreetmap.gui.jmapviewer.tilesources.OsmTileSource;
import org.openstreetmap.gui.jmapviewer.tilesources.OsmTileSource.TransportMap;

public class Driver {
	
	// components
	
	static JFrame frame;
	static JPanel animationPanel;
	static JPanel controlPanel;
	static JPanel settingsPanel;
	
	static JButton playButton;
	static JTextField timeText;
	static JSlider animationTime;
	static JCheckBox stopsCheck;
	static JComboBox<Integer> timePicker;
	
	static ButtonGroup heuristicGroup;
	static JRadioButton heur1;
	static JRadioButton heur2;
	
	static JTextField numStops;
	
	static JButton csvButton;
	static JButton raccImageButton;
	static JButton colorButton;
	
	static JButton saveImageButton;
	
	static JMapViewer map;
	static IconMarker racc;
	
	static Timer timer;
	
	static Color lineColor = Color.BLUE;
	
	// variables
	
	static int currTime = 0;
	static int animationSpeed = 15;
	static Integer[] animationTimes = {15, 30, 45, 60};
	
	// trip
	
	static ArrayList<TripPoint> trip;
	static String fileLocation = "triplog.csv";

    public static void main(String[] args) throws FileNotFoundException, IOException {
    	
    	try {
            TripPoint.readFile(fileLocation);
            TripPoint.h1StopDetection();
        } catch (IOException e) {
            System.out.println("Error reading trip log file: " + e.getMessage());
            return;
        }
    	
    	trip = TripPoint.getTrip();
    	
    	// set up frame
    	frame = new JFrame("Project5 Prototype");
    	
    	frame.setPreferredSize(new Dimension(1200, 800));
        
        // set up top panel for input selections
    	animationPanel = new JPanel();
    	controlPanel = new JPanel();
    	settingsPanel = new JPanel();
    	
    	//ANIMATION
    	
        // play button
    	playButton = new JButton("Play");
    	
    	// text to show current time
    	JTextField timeLabel = new JTextField("Time:");
    	timeLabel.setEditable(false);
    	timeLabel.setBorder(null);
    	
    	timeText = new JTextField("0:00:00");
    	timeText.setEditable(false);
    	
    	// slider to show animation frame
    	animationTime = new JSlider(0, trip.size()*5-5);
    	
    	animationTime.setPaintTicks(true);
    	animationTime.setPaintLabels(true);
    	
    	animationTime.setMajorTickSpacing((trip.size()*5-5)/4);
    	
    	animationTime.setMinorTickSpacing(1);
    	
    	animationTime.setSnapToTicks(true);
    	
    	animationTime.setValue(0);
    	
    	// CONTROLS
    	
        // checkbox to enable/disable stops
    	stopsCheck = new JCheckBox("Include Stops");
    	
    	stopsCheck.setSelected(true);
    	
        // dropbox to pick animation time
    	JTextField timePickerText = new JTextField("Animation Speed:");
    	timePickerText.setEditable(false);
    	timePickerText.setBorder(null);
    	timePicker = new JComboBox<Integer>(animationTimes);
    	
    	// heuristic radio buttons
    	heuristicGroup = new ButtonGroup();
    	
    	heur1 = new JRadioButton("h1");
    	heur2 = new JRadioButton("h2");
    	
    	heuristicGroup.add(heur1);
    	heuristicGroup.add(heur2);
    	
    	heur1.setSelected(true);
    	
    	numStops = new JTextField("# of Stops: ");
    	numStops.setEditable(false);
    	numStops.setBorder(null);
    	
    	// SETTINGS
    	
    	// csvSelecter
    	csvButton = new JButton(fileLocation);
    	
    	// racc image
    	BufferedImage img = null;
		try {
			
		    img = ImageIO.read(new File("raccoon.png"));
		    
		} catch (IOException e) {
			
		}
		
    	racc = new IconMarker(new Coordinate(trip.get(0).getLat(), trip.get(0).getLon()), img);
    	
    	ImageIcon raccImageIcon = new ImageIcon(img);
    	raccImageButton = new JButton(raccImageIcon);
    	
    	raccImageButton.setMaximumSize(raccImageButton.getSize());
    	
    	// color selector
    	colorButton = new JButton();
    	colorButton.setBackground(lineColor);
    	
    	colorButton.setPreferredSize(new Dimension(30, 30));
    	
    	// save image
    	saveImageButton = new JButton("Save as JPG");
        
        // add all to top panel
    	animationPanel.add(playButton);
    	animationPanel.add(animationTime);
    	animationPanel.add(timeLabel);
    	animationPanel.add(timeText);
    	
    	controlPanel.add(stopsCheck);
    	controlPanel.add(timePickerText);
    	controlPanel.add(timePicker);
    	controlPanel.add(heur1);
    	controlPanel.add(heur2);
    	controlPanel.add(numStops);
    	
    	settingsPanel.add(csvButton);
    	settingsPanel.add(raccImageButton);
    	settingsPanel.add(colorButton);
    	
        // set up mapViewer
    	map = new JMapViewer();
    	
    	//map.setTileSource(new TransportMap());
        map.setTileSource(new TransportMap());
        
    	map.setZoomControlsVisible(false);
    	map.removeMouseWheelListener(map.getMouseWheelListeners()[0]);
    	map.removeMouseListener(map.getMouseListeners()[0]);
    	
    	map.addMapMarker(racc);
        
        // add listeners
    	
    	playButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				
				if(playButton.getText()=="Play" && currTime!=trip.size()*5-5) {
					play();
				}else if(playButton.getText()=="Pause") {
					pause();
				}
				
			}
    		
    	});
    	
    	animationTime.addChangeListener(new ChangeListener() {
    	
    		@Override
		    public void stateChanged(ChangeEvent event) {
		       
    			if(currTime!=animationTime.getValue()) {
    				setTime(animationTime.getValue());
    			}
    			
		    }
		      
		});
    	
    	stopsCheck.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				
				toggleStops();
    			
			}
    		
    	});
    	
    	timePicker.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				
				animationSpeed = (int) timePicker.getSelectedItem();
				timer.setDelay(animationSpeed*1000 / trip.size());
				
			}
        	
        });
    	
    	JFileChooser fc = new JFileChooser();
    	
    	csvButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				
				int returnVal = fc.showOpenDialog(fc);

		        if (returnVal == JFileChooser.APPROVE_OPTION) {
		            File file = fc.getSelectedFile();
		            //This is where a real application would open the file.
		            setTrip(file.getName());
		        }
				
			}
    		
    	});
    	
    	raccImageButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				
				int returnVal = fc.showOpenDialog(fc);

		        if (returnVal == JFileChooser.APPROVE_OPTION) {
		            File file = fc.getSelectedFile();
		            //This is where a real application would open the file.
		            
		            try {
		                File pathToFile = file;
		                Image image = ImageIO.read(pathToFile);
			            
			            updateRacc(image);
		            } catch (IOException ex) {
		                ex.printStackTrace();
		            }
		        }
				
			}
    		
    	});
    	
    	colorButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				
				Color c = JColorChooser.showDialog(fc,"Select a trail color",lineColor);
				
				lineColor = c!=null ? c : lineColor;
				
				colorButton.setBackground(lineColor);
				
				setTime(currTime);
				
			}
    		
    	});
    	
    	saveImageButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				
				String name = saveImage();
				
				// create a dialog Box
	            JDialog d = new JDialog(frame, "Saved Image");
	 
	            // create a label
	            JLabel l = new JLabel(
	            		name!=null ? "Your image was saved as: " + name : "Your image could not be saved"
	            );
	 
	            d.add(l);
	 
	            // setsize of dialog
	            d.setSize(300, 100);
	 
	            // set visibility of dialog
	            d.setVisible(true);
	            
	            d.setLocationRelativeTo(null);
				
			}
    		
    	});
    	
    	heur1.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				
				try {
					
					if(heur1.isSelected()) {
						setNumStops(TripPoint.h1StopDetection());
					}else if(heur2.isSelected()) {
						setNumStops(TripPoint.h2StopDetection());
					}
					
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
				updateTrip();
				
			}
    		
    	});
       
        // set the map center and zoom level
    	
    	updateView();
    	
    	// finish frame
    	JPanel topPanel = new JPanel();
    	topPanel.add(animationPanel, BorderLayout.EAST);
    	topPanel.add(new JPanel(), BorderLayout.CENTER);
    	topPanel.add(controlPanel, BorderLayout.WEST);
    	
    	JPanel bottomPanel = new JPanel();
    	bottomPanel.add(settingsPanel, BorderLayout.EAST);
    	bottomPanel.add(new JPanel(), BorderLayout.CENTER);
    	bottomPanel.add(saveImageButton, BorderLayout.WEST);
    	
        frame.add(topPanel, BorderLayout.NORTH);
        frame.add(map, BorderLayout.CENTER);
        frame.add(bottomPanel, BorderLayout.SOUTH);
        
        frame.add(new JPanel(), BorderLayout.EAST);
        frame.add(new JPanel(), BorderLayout.WEST);
    	
    	frame.pack();
    	
    	frame.setLocationRelativeTo(null);
    	
        frame.setVisible(true);
        
        setTime(0);
        
        updateTrip();
        
    }
    
    public static void setTrip(String fileName) {
    	
    	// Read file and call stop detection
   	 	try {
            TripPoint.readFile(fileName);
            
            if(heur1.isSelected()) {
            	setNumStops(TripPoint.h1StopDetection());
            }else {
            	setNumStops(TripPoint.h2StopDetection());
            }
       	 	
       	 	fileLocation = fileName;
       	 	
       	 	csvButton.setText(fileName);
       	 	
       	 	updateTrip();
       	 	
       	 	updateView();
       	 	
       	 	setTime(0);
       	 	
        } catch (IOException e) {
            System.out.println("Error reading trip log file: " + e.getMessage());
            return;
        }
    	
    }
    
    public static void updateTrip() {
    	
    	trip = stopsCheck.isSelected() ? TripPoint.getTrip() : TripPoint.getMovingTrip();
    	
    	try {
    	
	    	if(heur1.isSelected()) {
	        	setNumStops(TripPoint.h1StopDetection());
	        }else {
	        	setNumStops(TripPoint.h2StopDetection());
	        }
    	
	    } catch (IOException e) {
	        System.out.println("Error reading trip log file: " + e.getMessage());
	        return;
	    }
    	
    }
    
    public static void updateView() {
    	
    	// Get Max Coordinates
    	double minLat = TripPoint.getTrip().get(0).getLat();
    	double maxLat = TripPoint.getTrip().get(0).getLat();
    	double minLon = TripPoint.getTrip().get(0).getLon();
    	double maxLon = TripPoint.getTrip().get(0).getLon();
    	
    	for(TripPoint t : TripPoint.getTrip()) {
    		if(t.getLat() < minLat) {
    			minLat = t.getLat();
    		}else if(t.getLat() > maxLat) {
    			maxLat = t.getLat();
    		}
    		
    		if(t.getLon() < minLon) {
    			minLon = t.getLon();
    		}else if(t.getLon() > maxLon) {
    			maxLon = t.getLon();
    		}
    	}
    
    	map.setDisplayPosition(new Coordinate((minLat + maxLat)/2, (minLon + maxLon)/2), 6);
    	
    }
    
    public static void setNumStops(int n) {
    	
    	numStops.setText("# of Stops: " + n);

    	System.out.println("Debug for Quynh");
    	System.out.println("# of Stops: " + n);
    	System.out.println(numStops.getText());
    	System.out.println();
    	
    }
    
    public static void play() {
    	
    	System.out.println("Play");
    	
    	playButton.setText("Pause");
    	
    	int timeDiff = animationSpeed*1000 / trip.size();
    	
    	ActionListener ticktock = new ActionListener() {
    			
    		public void actionPerformed(ActionEvent evnt) {
    					
    			if(currTime<=trip.size()*5-10) {
    				setTime(currTime + 5);
    			}else {
    				pause();
    			}
    				
    		}
    	};
    		
    	timer = new Timer(timeDiff, ticktock); //timer is ticking
    		
    	timer.start();
    	
    }
    
    public static void pause() {
    	
    	System.out.println("Pause");
    	
    	playButton.setText("Play");
    	
    	if(timer!=null) {
    		timer.stop();
    	}
    	
    }
    
    public static void setTime(int t) {
    	
    	timeText.setText(timeToString(t));
    	
    	currTime = t;
    	
    	animationTime.setValue(t);
    	
    	map.removeAllMapPolygons();
    	
    	if(t > 0) {
    		
    		for(int i = 1; i < t/5; i++) {
    			
    			TripPoint prev = trip.get(i-1);
    			TripPoint curr = trip.get(i);
    			
    			drawLine(prev, curr);
    			
    		}
    		
    	}
    	
    	moveRacc();
    	
    	if(t==trip.get(trip.size()-1).getTime()) {
    		pause();
    	}
    	
    }
    
    public static void drawLine(TripPoint t1, TripPoint t2) {
    	
    	Coordinate a = new Coordinate(t1.getLat(), t1.getLon());
		Coordinate b = new Coordinate(t2.getLat(), t2.getLon());
    	
    	//draw lines and move icon
    	List<Coordinate> route = new ArrayList<Coordinate>(Arrays.asList(a, b, a));
    	MapPolygonImpl mp = new MapPolygonImpl(route);
    	mp.setColor(lineColor);
    	map.addMapPolygon(mp);
    			
    	map.setMapPolygonsVisible(true);
    			
    	map.repaint();
    	
    }
    
    public static void moveRacc() {
    	
    	TripPoint t = trip.get(currTime/5);
    	
    	racc.setLat(t.getLat());
		racc.setLon(t.getLon());
    	
    }
    
    public static void updateRacc(Image i) {
    	
    	Image img = i.getScaledInstance(35, 35, 0);
    	
    	ImageIcon raccImageIcon = new ImageIcon(img);
    	
    	raccImageButton.setIcon(raccImageIcon);
    	
    	racc.setImage(img);    
    	
    }
    
    public static void toggleStops() {
    	
    	TripPoint curr = trip.get(currTime/5);
    	
    	updateTrip();

    	animationTime.setMaximum(trip.size()*5-5);
    	
    	for(int i = 0; i < trip.size(); i++) {
    		if(trip.get(i).equals(curr)) {
    			setTime(i*5);
    		}
    	}
    	
    }
    
    public static String timeToString(int time) {
    	
    	int days = (int)Math.floor(time/24/60);
    	int hours = (int)Math.floor(time/60%24);
    	int minutes = (int)Math.floor(time%60);
    	
    	return days + ":" + String.format("%02d", hours) + ":" + String.format("%02d", minutes);
    	
    }
    
    public static String saveImage() {
    	
    	JPanel panel = map;
    	
    	String name = fileLocation + ".jpg";
    	
    	BufferedImage img = new BufferedImage(panel.getWidth(), panel.getHeight(), BufferedImage.TYPE_INT_RGB);
        panel.print(img.getGraphics()); // or: panel.printAll(...);
        try {
            ImageIO.write(img, "jpg", new File(name));
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            
            return null;
        }

        return name;
    	
    }
    
}
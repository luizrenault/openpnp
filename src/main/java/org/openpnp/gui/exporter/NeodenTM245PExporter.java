package org.openpnp.gui.exporter;

import java.awt.FileDialog;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.border.TitledBorder;

import org.openpnp.gui.support.MessageBoxes;
import org.openpnp.machine.reference.feeder.ReferenceDragFeeder;
import org.openpnp.machine.reference.feeder.ReferenceTrayFeeder;
import org.openpnp.machine.reference.vision.ReferenceBottomVision;
import org.openpnp.model.Board;
import org.openpnp.model.BoardLocation;
import org.openpnp.model.Configuration;
import org.openpnp.model.LengthUnit;
import org.openpnp.model.Location;
import org.openpnp.model.Part;
import org.openpnp.model.Placement;
import org.openpnp.spi.Feeder;
import org.openpnp.spi.PartAlignment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.FormSpecs;
import com.jgoodies.forms.layout.RowSpec;

@SuppressWarnings("serial")
public class NeodenTM245PExporter implements BoardExporter {
    private final static String NAME = "Neoden TM245P";
    private final static String DESCRIPTION = "Export Neoden TM245P CSV Format.";
    private static final Logger logger = LoggerFactory.getLogger(NeodenTM245PExporter.class);

    FileWriter topFile;
	private File bottomFile;
	BoardLocation exportboardlocation;
	Board exportboard;

	@Override
	public boolean exportBoard(Frame parent, BoardLocation exportBoardLocation) throws Exception {
		exportboardlocation=exportBoardLocation;
		exportboard=exportBoardLocation.getBoard();
		logger.trace("Neoden TM245P Exporter");
        Dlg dlg = new Dlg(parent);
        dlg.setVisible(true);
		return true;
	}

	@Override
	public String getExporterName() {
		return NAME;
	}

	@Override
	public String getExporterDescription() {
		return DESCRIPTION;
	}

	
	class Dlg extends JDialog {
        private JTextField textFieldTopFile;
        private final Action browseTopFileAction = new SwingAction();
        private final Action importAction = new SwingAction_2();
        private final Action cancelAction = new SwingAction_3();
        private JCheckBox chckbxUsePickLocations;
        private JCheckBox chckbxCreateMissingParts;

        public Dlg(Frame parent) {
            super(parent, DESCRIPTION, true);
            getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

            JPanel panel = new JPanel();
            panel.setBorder(new TitledBorder(null, "Files", TitledBorder.LEADING, TitledBorder.TOP,
                    null, null));
            getContentPane().add(panel);
            panel.setLayout(new FormLayout(
                    new ColumnSpec[] {FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC,
                            FormSpecs.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"),
                            FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC,},
                    new RowSpec[] {FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
                            FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,}));

            JLabel lblTopFilemnt = new JLabel("Destination File (.csv)");
            panel.add(lblTopFilemnt, "2, 2, right, default");

            textFieldTopFile = new JTextField();
            panel.add(textFieldTopFile, "4, 2, fill, default");
            textFieldTopFile.setColumns(10);

            JButton btnBrowse = new JButton("Browse");
            btnBrowse.setAction(browseTopFileAction);
            panel.add(btnBrowse, "6, 2");

            JPanel panel_1 = new JPanel();
            panel_1.setBorder(new TitledBorder(null, "Options", TitledBorder.LEADING,
                    TitledBorder.TOP, null, null));
            getContentPane().add(panel_1);
            panel_1.setLayout(new FormLayout(
                    new ColumnSpec[] {FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC,},
                    new RowSpec[] {
                    		FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
                    		FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
                    		FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
                    		FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
                    }));

            chckbxUsePickLocations = new JCheckBox("Use Pick Locations as Pick Offset");
            chckbxUsePickLocations.setSelected(true);
            panel_1.add(chckbxUsePickLocations, "2, 2");

            chckbxCreateMissingParts = new JCheckBox("Create Missing Parts");
            chckbxCreateMissingParts.setSelected(true);
            panel_1.add(chckbxCreateMissingParts, "2, 4");

            JSeparator separator = new JSeparator();
            getContentPane().add(separator);

            JPanel panel_2 = new JPanel();
            FlowLayout flowLayout = (FlowLayout) panel_2.getLayout();
            flowLayout.setAlignment(FlowLayout.RIGHT);
            getContentPane().add(panel_2);

            JButton btnCancel = new JButton("Cancel");
            btnCancel.setAction(cancelAction);
            panel_2.add(btnCancel);

            JButton btnImport = new JButton("Export");
            btnImport.setAction(importAction);
            panel_2.add(btnImport);

            setSize(400, 400);
            setLocationRelativeTo(parent);

            JRootPane rootPane = getRootPane();
            KeyStroke stroke = KeyStroke.getKeyStroke("ESCAPE");
            InputMap inputMap = rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
            inputMap.put(stroke, "ESCAPE");
            rootPane.getActionMap().put("ESCAPE", cancelAction);
        }
        private class SwingAction extends AbstractAction {
            public SwingAction() {
                putValue(NAME, "Browse");
                putValue(SHORT_DESCRIPTION, "Browse");
            }

            public void actionPerformed(ActionEvent e) {
                FileDialog fileDialog = new FileDialog(Dlg.this);
                fileDialog.setFilenameFilter(new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String name) {
                        return false || name.toLowerCase().endsWith(".csv")
                                || name.toLowerCase().endsWith(".txt")
                                || name.toLowerCase().endsWith(".dat");
                    }
                });
                fileDialog.setMode(FileDialog.SAVE);
                fileDialog.setVisible(true);
                if (fileDialog.getFile() == null) {
                    return;
                }
                File file = new File(new File(fileDialog.getDirectory()), fileDialog.getFile());
                textFieldTopFile.setText(file.getAbsolutePath());
            }
        }



        private class SwingAction_2 extends AbstractAction {
            public SwingAction_2() {
                putValue(NAME, "Export");
                putValue(SHORT_DESCRIPTION, "Export");
            }

            public void actionPerformed(ActionEvent e) {
                logger.debug("Parsing " + textFieldTopFile.getText() + " CSV FIle");
                boolean firsttapefeeder=true;
                boolean firsttrayfeeder=true;
                try {
					topFile = new FileWriter(textFieldTopFile.getText());
	                BufferedWriter writer = new BufferedWriter(topFile);


	                List<Feeder> feeders = Configuration.get().getMachine().getFeeders();
	                List<PartAlignment> partaligment=Configuration.get().getMachine().getPartAlignments();
	                for (Feeder feeder:feeders){
	                	int number=Integer.parseInt(feeder.getName());
	                	if(number<1 || number>99){
	                		logger.trace("Feeder number out of range...");
	                		continue;
	                	}
	                	number+=10000;
	                	Location location=feeder.getPickLocation().convertToUnits(LengthUnit.Millimeters);
	                	Part part=feeder.getPart();

	                	if(part != null){
                			int x1=(int) (location.getX()*1000);
                			int y1=(int) (location.getY()*1000);
                			int x2=(int) (location.getX()*1000);
                			int y2=(int) (location.getY()*1000);
                			int pickdepth=(int) (location.getZ()*1000);
                			int placedepth=(int) (-part.getHeight().convertToUnits(LengthUnit.Millimeters).getValue()*1000);
                			int pickdelay=0;
                			int placedelay=0;
                			double sizexfp=part.getPackage().getFootprint().getBodyWidth();
                			double sizeyfp=part.getPackage().getFootprint().getBodyHeight();
                			int sizex=0;
                			int sizey=0;
                			switch(part.getPackage().getFootprint().getUnits()){
                			case Inches:
                				sizexfp*=25.4*1000;
                				sizeyfp*=25.4*1000;
                				break;
                			case Centimeters:
                				sizexfp*=10000;
                				sizeyfp*=10000;
                				break;
                			case Feet:
                				break;
                			case Meters:
                				sizexfp*=1000000;
                				sizeyfp*=1000000;
                				break;
                			case Millimeters:
                				sizexfp*=1000;
                				sizeyfp*=1000;
                				break;
                			default:
                				sizexfp*=1000;
                				sizeyfp*=1000;
                				break;
                			}
                			sizex=(int)sizexfp;
                			sizey=(int)sizeyfp;
                			int rate=0; 
                			int speed=(int) (part.getSpeed()*100);
                			int torque=60;
                			int vacuum1=-60;
                			int vacuum2=-60;
                			int vacuumoff=0;
                			int calibration=0;
                			int chipx=0;
                			int chipy=0;
                			int loopx=0;
                			int loopy=0;
                			
        	                if(partaligment.getClass()==ReferenceBottomVision.class){
        	                	ReferenceBottomVision rbv=(ReferenceBottomVision) partaligment;
        	                	calibration=rbv.getPartSettings(part).isEnabled()?1:0;
        	                }
                			int skip=feeder.isEnabled()?1:0;
                			
                			String spec=part.getId();

	                		if(feeder.getClass()==ReferenceDragFeeder.class){
	        	                if(firsttapefeeder){
	        	                	writer.write("#stack Id;X1;Y1;X2;Y2;Pick depth;Place depth;Pick delay;Place delay;Size X;Size Y;Rate;Speed;Torque;Vacuum 1;Vacuum 2;Vacuum Off;Calibration;Skip;Spec");
	        	                	writer.newLine();
	        	                	firsttapefeeder=false;
	        	                }
	                			ReferenceDragFeeder dragfeeder=(ReferenceDragFeeder) feeder;
	                			rate=(int) (dragfeeder.getFeedStartLocation().convertToUnits(LengthUnit.Millimeters).getLinearDistanceTo(dragfeeder.getFeedEndLocation().convertToUnits(LengthUnit.Millimeters))*1000);
	                		}
	                		if(feeder.getClass()==ReferenceTrayFeeder.class){
	        	                if(firsttrayfeeder){
	        	                	writer.write("#stack Id;X1;Y1;X2;Y2;Pick depth;Place depth;Pick delay;Place delay;Size X;Size Y;Rate;Speed;Torque;Vacuum 1;Vacuum 2;Vacuum Off;Calibration;Skip;Chip X;Chip Y;Loop X;Loop Y;Spec");
	        	                	writer.newLine();
	        	                	firsttrayfeeder=false;
	        	                }
	        	                ReferenceTrayFeeder referencetrayfeeder=(ReferenceTrayFeeder) feeder;
	        	                Location chip=referencetrayfeeder.getOffsets().convertToUnits(LengthUnit.Millimeters);
	        	                chipx=(int) (chip.getX()*1000);
	        	                chipy=(int) (chip.getY()*1000);
	        	                loopx=referencetrayfeeder.getTrayCountX();
	        	                loopy=referencetrayfeeder.getTrayCountY();
	                		}

                			writer.write(number + ";" );
	                		writer.write(x1 + ";" + y1 + ";");
                			writer.write(x2 + ";" + y2 + ";");
                			writer.write(pickdepth + ";");
                			writer.write(placedepth + ";");
                			writer.write(pickdelay + ";");
                			writer.write(placedelay + ";");
                			writer.write(sizex + ";");
                			writer.write(sizey + ";");
                			writer.write(rate + ";");
                			writer.write(speed + ";");
                			writer.write(torque + ";");
                			writer.write(vacuum1 + ";");
                			writer.write(vacuum2 + ";");
                			writer.write(vacuumoff + ";");
                			writer.write(calibration + ";");
                			writer.write(skip + ";");

	                		if(feeder.getClass()==ReferenceTrayFeeder.class){
	                			writer.write(chipx + ";");
	                			writer.write(chipy + ";");
	                			writer.write(loopx + ";");
	                			writer.write(loopy + ";");
	                		}
                			
                			writer.write(spec);
                			writer.newLine();
	                	}


	                }

                	writer.write("#Circuit;OffsetX;OffsetY;Skip");
                	writer.newLine();

                	writer.write("10201;");
                	Location offset=exportboardlocation.getLocation().convertToUnits(LengthUnit.Millimeters);
                	
                	writer.write((int)(offset.getX()*1000) + ";");
                	writer.write((int)(offset.getY()*1000) + ";");
                	writer.write(exportboardlocation.isEnabled()?"0":"1");
                	writer.newLine();

                	writer.write("#Component;Nozzle;Stack;X;Y;Angle;Skip;Name");
                	writer.newLine();
                	writer.write("0;100");
                	writer.newLine();

                	List<Placement> placements=exportboard.getPlacements();
                	int number=1;
                	for(Placement placement:placements){
                		if(placement.getPart()!=null){
                			if(exportboardlocation.getSide()!=placement.getSide()){
                				continue;
                			}
                    		Feeder feeder=Configuration.get().getMachine().getFeeder(placement.getPart().getId());
	                		writer.write(number + ";");
	                		writer.write("1;");
                    		if(feeder!=null){
		                		writer.write(feeder.getId());
                    		} else {
		                		writer.write("0;");
                    		}
	                		Location pos=placement.getLocation().rotateXy(exportboardlocation.getLocation().getRotation()).convertToUnits(LengthUnit.Millimeters);
	                		int angle=(int)(pos.getRotation());
	                		angle+=exportboardlocation.getLocation().getRotation();
	                		angle%=360;
	                		if(angle>180){
	                			angle-=360;
	                		}
	                		if(angle<-180){
	                			angle+=360;
	                		}
	                		
	                		writer.write((int)(pos.getX()*1000) + ";" + (int)(pos.getY()*1000) + ";" + angle + ";");
	                		writer.write(((placement.getType()==Placement.Type.Place)?"0":"1") + ";");
	                		writer.write(placement.getId());
	                    	writer.newLine();
                		}
                	}
	                	
	                writer.close();
                } catch (Exception e1) {
                  MessageBoxes.errorBox(Dlg.this, "Export Error", e1);
				}
//                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));

//                board = new Board();
//                List<Placement> placements = new ArrayList<>();
//                try {
//                    if (topFile.exists()) {
//                        placements
//                                .addAll(parseFile(topFile, chckbxCreateMissingParts.isSelected()));
//                    }
//                }
//                catch (Exception e1) {
//                    MessageBoxes.errorBox(Dlg.this, "Import Error", e1);
//                    return;
//                }
//                for (Placement placement : placements) {
//                    board.addPlacement(placement);
//                }
                setVisible(false);
            }
        }

        private class SwingAction_3 extends AbstractAction {
            public SwingAction_3() {
                putValue(NAME, "Cancel");
                putValue(SHORT_DESCRIPTION, "Cancel");
            }

            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        }
	}
}

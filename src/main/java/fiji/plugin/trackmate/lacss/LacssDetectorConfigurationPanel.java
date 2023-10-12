package fiji.plugin.trackmate.lacss;

import static fiji.plugin.trackmate.lacss.LacssDetectorFactory.KEY_LACSS_CUSTOM_MODEL_FILEPATH;
import static fiji.plugin.trackmate.lacss.LacssDetectorFactory.KEY_LACSS_MODEL;
import static fiji.plugin.trackmate.lacss.LacssDetectorFactory.KEY_LACSS_PYTHON_FILEPATH;
import static fiji.plugin.trackmate.lacss.LacssDetectorFactory.KEY_CELL_DIAMETER;
import static fiji.plugin.trackmate.lacss.LacssDetectorFactory.KEY_LOGGER;
import static fiji.plugin.trackmate.lacss.LacssDetectorFactory.KEY_OPTIONAL_CHANNEL_2;
import static fiji.plugin.trackmate.lacss.LacssDetectorFactory.KEY_USE_GPU;
import static fiji.plugin.trackmate.detection.DetectorKeys.KEY_TARGET_CHANNEL;
import static fiji.plugin.trackmate.detection.ThresholdDetectorFactory.KEY_SIMPLIFY_CONTOURS;
import static fiji.plugin.trackmate.gui.Fonts.BIG_FONT;
import static fiji.plugin.trackmate.gui.Fonts.FONT;
import static fiji.plugin.trackmate.gui.Fonts.SMALL_FONT;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import fiji.plugin.trackmate.Logger;
import fiji.plugin.trackmate.Model;
import fiji.plugin.trackmate.Settings;
import fiji.plugin.trackmate.lacss.LacssSettings.PretrainedModel;
import fiji.plugin.trackmate.gui.components.ConfigurationPanel;
import fiji.plugin.trackmate.util.DetectionPreview;
import fiji.plugin.trackmate.util.FileChooser;
import fiji.plugin.trackmate.util.FileChooser.DialogType;
import fiji.plugin.trackmate.util.FileChooser.SelectionMode;

public class LacssDetectorConfigurationPanel extends ConfigurationPanel
{

	private static final long serialVersionUID = 1L;

	private static final String TITLE = LacssDetectorFactory.NAME;

	private static final ImageIcon ICON = LacssUtils.logo64();

	private static final NumberFormat DIAMETER_FORMAT = new DecimalFormat( "#.#" );

	protected static final String DOC1_URL = "https://imagej.net/plugins/trackmate/trackmate-cellpose";

	private final JButton btnBrowseLacssPath;

	private final JTextField tfLacssExecutable;

	private final JComboBox< PretrainedModel > cmbboxPretrainedModel;

	private final JComboBox< String > cmbboxCh1;

	private final JComboBox< String > cmbboxCh2;

	private final JFormattedTextField ftfDiameter;

	private final JCheckBox chckbxSimplify;

	private final Logger logger;

	private final JCheckBox chckbxUseGPU;

	private final JTextField tfCustomPath;

	private final JButton btnBrowseCustomModel;

	public LacssDetectorConfigurationPanel( final Settings settings, final Model model )
	{
		this.logger = model.getLogger();

		final GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.rowWeights = new double[] { 0., 0., 0., 0., 0., 0., 0., 0., 0., 0., 0., .1 };
		gridBagLayout.columnWidths = new int[] { 144, 0, 32 };
		gridBagLayout.columnWeights = new double[] { 1.0, 1.0, 0.0 };
		setLayout( gridBagLayout );

		final JLabel lblDetector = new JLabel( TITLE, ICON, JLabel.RIGHT );
		lblDetector.setFont( BIG_FONT );
		lblDetector.setHorizontalAlignment( SwingConstants.CENTER );
		final GridBagConstraints gbcLblDetector = new GridBagConstraints();
		gbcLblDetector.gridwidth = 3;
		gbcLblDetector.insets = new Insets( 0, 5, 5, 0 );
		gbcLblDetector.fill = GridBagConstraints.HORIZONTAL;
		gbcLblDetector.gridx = 0;
		gbcLblDetector.gridy = 0;
		add( lblDetector, gbcLblDetector );

		final String text = "Click here for the documentation";
		final JLabel lblUrl = new JLabel( text );
		lblUrl.setHorizontalAlignment( SwingConstants.CENTER );
		lblUrl.setForeground( Color.BLUE.darker() );
		lblUrl.setFont( FONT.deriveFont( Font.ITALIC ) );
		lblUrl.setCursor( new Cursor( Cursor.HAND_CURSOR ) );
		lblUrl.addMouseListener( new MouseAdapter()
		{
			@Override
			public void mouseClicked( final java.awt.event.MouseEvent e )
			{
				try
				{
					Desktop.getDesktop().browse( new URI( DOC1_URL ) );
				}
				catch ( URISyntaxException | IOException ex )
				{
					ex.printStackTrace();
				}
			}

			@Override
			public void mouseExited( final java.awt.event.MouseEvent e )
			{
				lblUrl.setText( text );
			}

			@Override
			public void mouseEntered( final java.awt.event.MouseEvent e )
			{
				lblUrl.setText( "<html><a href=''>" + DOC1_URL + "</a></html>" );
			}
		} );
		final GridBagConstraints gbcLblUrl = new GridBagConstraints();
		gbcLblUrl.fill = GridBagConstraints.HORIZONTAL;
		gbcLblUrl.gridwidth = 3;
		gbcLblUrl.insets = new Insets( 0, 10, 5, 15 );
		gbcLblUrl.gridx = 0;
		gbcLblUrl.gridy = 1;
		add( lblUrl, gbcLblUrl );

		/*
		 * Path to Python or Lacss.
		 */

		final JLabel lblCusstomModelFile = new JLabel( "Path to Lacss / python executable:" );
		lblCusstomModelFile.setFont( FONT );
		final GridBagConstraints gbcLblCusstomModelFile = new GridBagConstraints();
		gbcLblCusstomModelFile.gridwidth = 2;
		gbcLblCusstomModelFile.anchor = GridBagConstraints.SOUTHWEST;
		gbcLblCusstomModelFile.insets = new Insets( 0, 5, 5, 5 );
		gbcLblCusstomModelFile.gridx = 0;
		gbcLblCusstomModelFile.gridy = 2;
		add( lblCusstomModelFile, gbcLblCusstomModelFile );

		btnBrowseLacssPath = new JButton( "Browse" );
		btnBrowseLacssPath.setFont( FONT );
		final GridBagConstraints gbcBtnBrowseLacssPath = new GridBagConstraints();
		gbcBtnBrowseLacssPath.insets = new Insets( 0, 0, 5, 0 );
		gbcBtnBrowseLacssPath.anchor = GridBagConstraints.SOUTHEAST;
		gbcBtnBrowseLacssPath.gridx = 2;
		gbcBtnBrowseLacssPath.gridy = 2;
		add( btnBrowseLacssPath, gbcBtnBrowseLacssPath );

		tfLacssExecutable = new JTextField( "" );
		tfLacssExecutable.setFont( SMALL_FONT );
		final GridBagConstraints gbcTfLacss = new GridBagConstraints();
		gbcTfLacss.gridwidth = 3;
		gbcTfLacss.insets = new Insets( 0, 5, 5, 0 );
		gbcTfLacss.fill = GridBagConstraints.BOTH;
		gbcTfLacss.gridx = 0;
		gbcTfLacss.gridy = 3;
		add( tfLacssExecutable, gbcTfLacss );
		tfLacssExecutable.setColumns( 15 );

		/*
		 * Custom model.
		 */

		final JLabel lblPathToCustomModel = new JLabel( "Path to custom model:" );
		lblPathToCustomModel.setFont( new Font( "Arial", Font.PLAIN, 10 ) );
		final GridBagConstraints gbcLblPathToCustomModel = new GridBagConstraints();
		gbcLblPathToCustomModel.anchor = GridBagConstraints.SOUTHWEST;
		gbcLblPathToCustomModel.gridwidth = 2;
		gbcLblPathToCustomModel.insets = new Insets( 0, 5, 5, 5 );
		gbcLblPathToCustomModel.gridx = 0;
		gbcLblPathToCustomModel.gridy = 4;
		add( lblPathToCustomModel, gbcLblPathToCustomModel );

		btnBrowseCustomModel = new JButton( "Browse" );
		btnBrowseCustomModel.setFont( new Font( "Arial", Font.PLAIN, 10 ) );
		final GridBagConstraints gbcBtnBrowseCustomModel = new GridBagConstraints();
		gbcBtnBrowseCustomModel.insets = new Insets( 0, 0, 5, 0 );
		gbcBtnBrowseCustomModel.anchor = GridBagConstraints.SOUTHEAST;
		gbcBtnBrowseCustomModel.gridx = 2;
		gbcBtnBrowseCustomModel.gridy = 4;
		add( btnBrowseCustomModel, gbcBtnBrowseCustomModel );

		tfCustomPath = new JTextField( " " );
		tfCustomPath.setFont( new Font( "Arial", Font.PLAIN, 10 ) );
		tfCustomPath.setColumns( 15 );
		final GridBagConstraints gbcTfCustomPath = new GridBagConstraints();
		gbcTfCustomPath.gridwidth = 3;
		gbcTfCustomPath.insets = new Insets( 0, 5, 5, 0 );
		gbcTfCustomPath.fill = GridBagConstraints.BOTH;
		gbcTfCustomPath.gridx = 0;
		gbcTfCustomPath.gridy = 5;
		add( tfCustomPath, gbcTfCustomPath );

		/*
		 * Pretrained model.
		 */

		final JLabel lblPretrainedModel = new JLabel( "Pretrained model:" );
		lblPretrainedModel.setFont( SMALL_FONT );
		final GridBagConstraints gbcLblPretrainedModel = new GridBagConstraints();
		gbcLblPretrainedModel.anchor = GridBagConstraints.EAST;
		gbcLblPretrainedModel.insets = new Insets( 0, 5, 5, 5 );
		gbcLblPretrainedModel.gridx = 0;
		gbcLblPretrainedModel.gridy = 6;
		add( lblPretrainedModel, gbcLblPretrainedModel );

		cmbboxPretrainedModel = new JComboBox<>( new Vector<>( Arrays.asList( PretrainedModel.values() ) ) );
		cmbboxPretrainedModel.setFont( SMALL_FONT );
		final GridBagConstraints gbcCmbboxPretrainedModel = new GridBagConstraints();
		gbcCmbboxPretrainedModel.gridwidth = 2;
		gbcCmbboxPretrainedModel.insets = new Insets( 0, 5, 5, 0 );
		gbcCmbboxPretrainedModel.fill = GridBagConstraints.HORIZONTAL;
		gbcCmbboxPretrainedModel.gridx = 1;
		gbcCmbboxPretrainedModel.gridy = 6;
		add( cmbboxPretrainedModel, gbcCmbboxPretrainedModel );

		/*
		 * Channel 1
		 */

		final JLabel lblSegmentInChannel = new JLabel( "Channel to segment:" );
		lblSegmentInChannel.setFont( SMALL_FONT );
		final GridBagConstraints gbcLblSegmentInChannel = new GridBagConstraints();
		gbcLblSegmentInChannel.anchor = GridBagConstraints.EAST;
		gbcLblSegmentInChannel.insets = new Insets( 0, 5, 5, 5 );
		gbcLblSegmentInChannel.gridx = 0;
		gbcLblSegmentInChannel.gridy = 7;
		add( lblSegmentInChannel, gbcLblSegmentInChannel );

		final List< String > l1 = Arrays.asList(
				"0: grayscale",
				"1: red",
				"2: green",
				"3: blue" );
		cmbboxCh1 = new JComboBox<>( new Vector<>( l1 ) );
		cmbboxCh1.setFont( SMALL_FONT );
		final GridBagConstraints gbcSpinner = new GridBagConstraints();
		gbcSpinner.fill = GridBagConstraints.HORIZONTAL;
		gbcSpinner.gridwidth = 2;
		gbcSpinner.insets = new Insets( 0, 5, 5, 0 );
		gbcSpinner.gridx = 1;
		gbcSpinner.gridy = 7;
		add( cmbboxCh1, gbcSpinner );

		/*
		 * Channel 2.
		 */

		final JLabel lblSegmentInChannelOptional = new JLabel( "Optional second channel:" );
		lblSegmentInChannelOptional.setFont( SMALL_FONT );
		final GridBagConstraints gbcLblSegmentInChannelOptional = new GridBagConstraints();
		gbcLblSegmentInChannelOptional.anchor = GridBagConstraints.EAST;
		gbcLblSegmentInChannelOptional.insets = new Insets( 0, 5, 5, 5 );
		gbcLblSegmentInChannelOptional.gridx = 0;
		gbcLblSegmentInChannelOptional.gridy = 8;
		add( lblSegmentInChannelOptional, gbcLblSegmentInChannelOptional );

		final List< String > l2 = Arrays.asList(
				"0: none",
				"1: red",
				"2: green",
				"3: blue" );
		cmbboxCh2 = new JComboBox<>( new Vector<>( l2 ) );
		cmbboxCh2.setFont( SMALL_FONT );
		final GridBagConstraints gbcSpinnerCh2 = new GridBagConstraints();
		gbcSpinnerCh2.fill = GridBagConstraints.HORIZONTAL;
		gbcSpinnerCh2.gridwidth = 2;
		gbcSpinnerCh2.insets = new Insets( 0, 5, 5, 0 );
		gbcSpinnerCh2.gridx = 1;
		gbcSpinnerCh2.gridy = 8;
		add( cmbboxCh2, gbcSpinnerCh2 );

		/*
		 * Diameter.
		 */

		final JLabel lblDiameter = new JLabel( "Cell diameter:" );
		lblDiameter.setFont( SMALL_FONT );
		final GridBagConstraints gbcLblDiameter = new GridBagConstraints();
		gbcLblDiameter.anchor = GridBagConstraints.EAST;
		gbcLblDiameter.insets = new Insets( 0, 5, 5, 5 );
		gbcLblDiameter.gridx = 0;
		gbcLblDiameter.gridy = 9;
		add( lblDiameter, gbcLblDiameter );

		ftfDiameter = new JFormattedTextField( DIAMETER_FORMAT );
		ftfDiameter.setHorizontalAlignment( SwingConstants.CENTER );
		ftfDiameter.setFont( SMALL_FONT );
		final GridBagConstraints gbcFtfDiameter = new GridBagConstraints();
		gbcFtfDiameter.insets = new Insets( 0, 5, 5, 5 );
		gbcFtfDiameter.fill = GridBagConstraints.HORIZONTAL;
		gbcFtfDiameter.gridx = 1;
		gbcFtfDiameter.gridy = 9;
		add( ftfDiameter, gbcFtfDiameter );

		final JLabel lblSpaceUnits = new JLabel( model.getSpaceUnits() );
		lblSpaceUnits.setFont( SMALL_FONT );
		final GridBagConstraints gbcLblSpaceUnits = new GridBagConstraints();
		gbcLblSpaceUnits.insets = new Insets( 0, 5, 5, 0 );
		gbcLblSpaceUnits.gridx = 2;
		gbcLblSpaceUnits.gridy = 9;
		add( lblSpaceUnits, gbcLblSpaceUnits );

		chckbxUseGPU = new JCheckBox( "Use GPU:" );
		chckbxUseGPU.setHorizontalTextPosition( SwingConstants.LEFT );
		chckbxUseGPU.setFont( SMALL_FONT );
		final GridBagConstraints gbcChckbxUseGPU = new GridBagConstraints();
		gbcChckbxUseGPU.anchor = GridBagConstraints.EAST;
		gbcChckbxUseGPU.insets = new Insets( 0, 0, 0, 5 );
		gbcChckbxUseGPU.gridx = 0;
		gbcChckbxUseGPU.gridy = 10;
		add( chckbxUseGPU, gbcChckbxUseGPU );

		/*
		 * Preview.
		 */

		final GridBagConstraints gbcBtnPreview = new GridBagConstraints();
		gbcBtnPreview.gridwidth = 3;
		gbcBtnPreview.fill = GridBagConstraints.BOTH;
		gbcBtnPreview.insets = new Insets( 0, 5, 5, 5 );
		gbcBtnPreview.gridx = 0;
		gbcBtnPreview.gridy = 16;

		final DetectionPreview detectionPreview = DetectionPreview.create()
				.model( model )
				.settings( settings )
				.detectorFactory( new LacssDetectorFactory<>() )
				.detectionSettingsSupplier( () -> getSettings() )
				.axisLabel( "Area histogram" )
				.get();
		add( detectionPreview.getPanel(), gbcBtnPreview );

		chckbxSimplify = new JCheckBox( "Simplify contours:" );
		chckbxSimplify.setHorizontalTextPosition( SwingConstants.LEFT );
		chckbxSimplify.setFont( SMALL_FONT );
		final GridBagConstraints gbcChckbxSimplify = new GridBagConstraints();
		gbcChckbxSimplify.anchor = GridBagConstraints.EAST;
		gbcChckbxSimplify.gridwidth = 2;
		gbcChckbxSimplify.insets = new Insets( 0, 5, 0, 0 );
		gbcChckbxSimplify.gridx = 1;
		gbcChckbxSimplify.gridy = 10;
		add( chckbxSimplify, gbcChckbxSimplify );

		/*
		 * Listeners and specificities.
		 */

		final ItemListener l3 = e -> {
			final boolean isCustom = cmbboxPretrainedModel.getSelectedItem() == PretrainedModel.CUSTOM;
			tfCustomPath.setVisible( isCustom );
			lblPathToCustomModel.setVisible( isCustom );
			btnBrowseCustomModel.setVisible( isCustom );
		};
		cmbboxPretrainedModel.addItemListener( l3 );
		l3.itemStateChanged( null );

		btnBrowseLacssPath.addActionListener( l -> browseLacssPath() );
		btnBrowseCustomModel.addActionListener( l -> browseCustomModelPath() );
	}

	protected void browseCustomModelPath()
	{
		btnBrowseCustomModel.setEnabled( false );
		try
		{
			final File file = FileChooser.chooseFile( this, tfCustomPath.getText(), null,
					"Browse to a Lacss custom model", DialogType.LOAD, SelectionMode.FILES_ONLY );
			if ( file != null )
				tfCustomPath.setText( file.getAbsolutePath() );
		}
		finally
		{
			btnBrowseCustomModel.setEnabled( true );
		}
	}

	protected void browseLacssPath()
	{
		btnBrowseLacssPath.setEnabled( false );
		try
		{
			final File file = FileChooser.chooseFile( this, tfLacssExecutable.getText(), null,
					"Browse to the Lacss Python executable", DialogType.LOAD, SelectionMode.FILES_ONLY );
			if ( file != null )
				tfLacssExecutable.setText( file.getAbsolutePath() );
		}
		finally
		{
			btnBrowseLacssPath.setEnabled( true );
		}
	}

	@Override
	public void setSettings( final Map< String, Object > settings )
	{
		tfLacssExecutable.setText( ( String ) settings.get( KEY_LACSS_PYTHON_FILEPATH ) );
		tfCustomPath.setText( ( String ) settings.get( KEY_LACSS_CUSTOM_MODEL_FILEPATH ) );
		cmbboxPretrainedModel.setSelectedItem( settings.get( KEY_LACSS_MODEL ) );
		cmbboxCh1.setSelectedIndex( ( int ) settings.get( KEY_TARGET_CHANNEL ) );
		cmbboxCh2.setSelectedIndex( ( int ) settings.get( KEY_OPTIONAL_CHANNEL_2 ) );
		ftfDiameter.setValue( settings.get( KEY_CELL_DIAMETER ) );
		chckbxUseGPU.setSelected( ( boolean ) settings.get( KEY_USE_GPU ) );
		chckbxSimplify.setSelected( ( boolean ) settings.get( KEY_SIMPLIFY_CONTOURS ) );
	}

	@Override
	public Map< String, Object > getSettings()
	{
		final HashMap< String, Object > settings = new HashMap<>( 9 );

		settings.put( KEY_LACSS_PYTHON_FILEPATH, tfLacssExecutable.getText() );
		settings.put( KEY_LACSS_CUSTOM_MODEL_FILEPATH, tfCustomPath.getText() );
		settings.put( KEY_LACSS_MODEL, cmbboxPretrainedModel.getSelectedItem() );

		settings.put( KEY_TARGET_CHANNEL, cmbboxCh1.getSelectedIndex() );
		settings.put( KEY_OPTIONAL_CHANNEL_2, cmbboxCh2.getSelectedIndex() );

		final double diameter = ( ( Number ) ftfDiameter.getValue() ).doubleValue();
		settings.put( KEY_CELL_DIAMETER, diameter );
		settings.put( KEY_SIMPLIFY_CONTOURS, chckbxSimplify.isSelected() );
		settings.put( KEY_USE_GPU, chckbxUseGPU.isSelected() );

		settings.put( KEY_LOGGER, logger );

		return settings;
	}

	@Override
	public void clean()
	{} 
}

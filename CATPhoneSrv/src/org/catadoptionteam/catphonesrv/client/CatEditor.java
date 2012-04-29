/**
 * 
 */
package org.catadoptionteam.catphonesrv.client;

import java.util.Date;

import org.catadoptionteam.catphonesrv.shared.FieldVerifier;
import org.catadoptionteam.catphonesrv.shared.Util;
import org.catadoptionteam.catphonesrv.shared.Cat;
import org.catadoptionteam.catphonesrv.shared.Cat.Felineality;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.datepicker.client.DateBox;

/**
 * @author bessermt
 *
 */
public class CatEditor extends Composite implements HasText
{

	private static CatRecordUiBinder uiBinder = GWT.create(CatRecordUiBinder.class);

	interface CatRecordUiBinder extends UiBinder<Widget, CatEditor> {}

	/**
	 * Because this class has a default constructor, it can
	 * be used as a binder template. In other words, it can be used in other
	 * *.ui.xml files as follows:
	 * <ui:UiBinder xmlns:ui="urn:ui:com.google.gwt.uibinder"
	 *   xmlns:g="urn:import:**user's package**">
	 *  <g:**UserClassName**>Hello!</g:**UserClassName>
	 * </ui:UiBinder>
	 * Note that depending on the widget that is used, it may be necessary to
	 * implement HasHTML instead of HasText.
	 */

	@UiField TextBox petIDTextBox;
	@UiField TextBox nameTextBox;
	@UiField CheckBox adoptedCheckBox;
	@UiField DateBox DOBDateBox;
	@UiField ListBox sexListBox;
	@UiField ListBox felinealityListBox;
	@UiField TextArea biographyTextArea;
	@UiField TextBox lgPhotoURLTextBox;
	@UiField TextBox smPhotoURLTextBox;
	@UiField TextBox extraIDTextBox;
	@UiField TextBox extraURLTextBox;
	@UiField Button okButton;
	@UiField Button cancelButton;

	private Long id_;
	private String expDate_;

	private CatEditor()
	{
		initWidget(uiBinder.createAndBindUi(this));
		// Can access @UiField after calling createAndBindUi
	}

	public CatEditor(final Cat cat)
	{
		this();

		id_ = cat.getID();

		expDate_ = cat.getExpDate();

		petIDTextBox.setText(cat.getPetID());

		nameTextBox.setText(cat.getName());

		adoptedCheckBox.setValue(cat.isAdopted());

		final DateBox.Format shortFormat = new DateBox.DefaultFormat(DateTimeFormat.getShortDateFormat()); //  // Deprecated, but I don't see a viable alternative.  
		DOBDateBox.setFormat(shortFormat);
		final String dob = cat.getDOB();
		if (dob != null)
		{
			final Date dobDate = Util.parseDate(dob);
			DOBDateBox.setValue(dobDate);
		}

		for (Cat.Sex sexValue: Cat.Sex.values())
		{
			final String sexValueString = sexValue.toString();
			sexListBox.addItem(sexValueString);
		}
		sexListBox.setSelectedIndex(cat.getSex().valueOf());

		final String[] Felinealities = cat.getFelinealityStringArray();
		for (String item: Felinealities)
		{
			felinealityListBox.addItem(item);
		}

		int index = -1;
		final Felineality felineality = cat.getFelineality();
		if (felineality != null)
		{
			index = felineality.valueOf();
		}
		felinealityListBox.setSelectedIndex(index);

		biographyTextArea.setText(cat.getBiography());

		lgPhotoURLTextBox.setText(cat.getLgPhotoURL());

		smPhotoURLTextBox.setText(cat.getSmPhotoURL());

		extraIDTextBox.setText(cat.getExtraID());

		extraURLTextBox.setText(cat.getExtraURL());
	}

	@UiHandler("cancelButton")
	void onCancelClick(ClickEvent e)
	{
		// TODO: ???
	}

	@UiHandler("okButton")
	void onOKClick(ClickEvent e)
	{
		// TODO: ???
	}

	public Button getOKButton()
	{
		final Button result = okButton;
		return result;
	}

	public Button getCancelButton()
	{
		final Button result = cancelButton;
		return result;
	}

	public void setText(String text)
	{
		// button.setText(text);
	}

	/**
	 * Gets invoked when the default constructor is called
	 * and a string is provided in the ui.xml file.
	 */
	public String getText()
	{
		final String result = null; // button.getText();
		return result;
	}

	public Cat getCat()
	{
		Cat result = null;

		final String petID = petIDTextBox.getText().trim();
		final String name = nameTextBox.getText().trim();
		final int category = Cat.CATEGORY_INVALID;
		final Boolean adopted = adoptedCheckBox.getValue();
		String dob = null;
		final Date dobDate = DOBDateBox.getValue();
		final String dobString = Util.toString(dobDate);
		if (dobString != null)
		{
			dob = dobString.trim();
		}
		final Cat.Sex sex = Cat.Sex.create(sexListBox.getSelectedIndex());
		final String biography = Util.norm(biographyTextArea.getText().trim());
		final String lgPhotoURL = lgPhotoURLTextBox.getText().trim();
		final String smPhotoURL = smPhotoURLTextBox.getText().trim();
		final String extraID = extraIDTextBox.getText().trim();
		final String extraURL = extraURLTextBox.getText().trim();

		final Felineality felineality = Felineality.create(felinealityListBox.getSelectedIndex());

		final boolean isGood = 
			FieldVerifier.isText(petID) && 
			FieldVerifier.isText(name) && 
			FieldVerifier.isDate(dob) && 
			(sex == Cat.Sex.MALE || sex == Cat.Sex.FEMALE) && 
			Cat.Felineality.isValid(felineality) && 
			FieldVerifier.isText(biography) && 
			FieldVerifier.isPhotoURL(lgPhotoURL) && 
			FieldVerifier.isPhotoURL(smPhotoURL);

		if (isGood)
		{
			result = new Cat(id_, petID, name, category, expDate_, adopted, dob, sex, felineality, biography, lgPhotoURL, smPhotoURL, extraID, extraURL);
		}

		return result;
	}
}

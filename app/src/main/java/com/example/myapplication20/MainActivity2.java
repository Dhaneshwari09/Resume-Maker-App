package com.example.myapplication20;

import static java.nio.file.Files.isReadable;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.LineSeparator;
import com.skydoves.colorpickerview.ColorPickerDialog;
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity2 extends AppCompatActivity {

    private String selectedShape = "rectangle"; // Default shape is rectangle
    private Button selectShapeButton;

    private SeekBar fontSizeSeekBar;
    private int selectedFontSize = 14;  // Default font size for PDF content
    private BaseColor pdfBackgroundColor = BaseColor.WHITE;
    private BaseColor pdfTextColor = BaseColor.BLACK;
    private static final int PICK_IMAGE = 100;
    private static final int REQUEST_STORAGE_PERMISSION = 1;

    private EditText nameEditText, emailEditText, phoneEditText, summaryEditText, skillsEditText, projectsEditText, achievementsEditText, educationEditText , exeperienceEditText;
    private ImageView photoImageView;
    private Button selectPhotoButton, generatePdfButton , newGeneratePdfButton , changeBgColorButton, changeTextColorButton;
    private Uri selectedImageUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeUI();
        selectPhotoButton.setOnClickListener(view -> openGallery());             // open gallery
        selectShapeButton.setOnClickListener(view -> openShapePickerDialog()); // Handle shape selection

        generatePdfButton.setOnClickListener(view -> generatePDF());           // genrate pdf
        newGeneratePdfButton.setOnClickListener(view -> resetForm());           // reset

        changeBgColorButton.setOnClickListener(view -> openColorPicker(true));          // change background color
        changeTextColorButton.setOnClickListener(view -> openColorPicker(false));       // change text color

        requestStoragePermission();

        //------- imageClick
        photoImageView.setOnClickListener(view -> {
            if (selectedImageUri != null) {
                new AlertDialog.Builder(this)
                        .setTitle("Remove Photo")
                        .setMessage("Are you sure you want to remove this photo?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            selectedImageUri = null;
                            photoImageView.setImageResource(R.drawable.insertimageee); // Replace with a default image
                            Toast.makeText(this, "Photo Removed!", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("No", null)
                        .show();
            } else {
                openGallery();
            }
        });
    }
    private void initializeUI() {
        //------- view
        nameEditText = findViewById(R.id.nameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        phoneEditText = findViewById(R.id.phoneEditText);
        summaryEditText = findViewById(R.id.summaryEditText);
        skillsEditText = findViewById(R.id.skillsEditText);
        projectsEditText = findViewById(R.id.projectsEditText);
        achievementsEditText = findViewById(R.id.achievementsEditText);
        educationEditText = findViewById(R.id.educationEditText);
        exeperienceEditText = findViewById(R.id.exeperienceEditText);
        photoImageView = findViewById(R.id.photoImageView);
        selectPhotoButton = findViewById(R.id.selectPhotoButton);
        selectShapeButton = findViewById(R.id.selectShapeButton);
        generatePdfButton = findViewById(R.id.generatePdfButton);
        newGeneratePdfButton = findViewById(R.id.newGeneratePdfButton);
        changeBgColorButton = findViewById(R.id.changeBgColorButton);
        changeTextColorButton = findViewById(R.id.changeTextColorButton);

        // Initialize the SeekBar and set its listener
        fontSizeSeekBar = findViewById(R.id.fontSizeSeekBar);
        selectedFontSize = 14;  // Set the default font size to 14
        fontSizeSeekBar.setProgress(selectedFontSize - 1);
        fontSizeSeekBar.setMax(99);

        // SeekBar listener to handle font size changes
        fontSizeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    selectedFontSize = progress + 1;
                    //   Toast.makeText(MainActivity2.this, "Font Size: " + selectedFontSize, Toast.LENGTH_SHORT).show();
                    updateFontSize(selectedFontSize);
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }
    // Method to open a shape picker dialog
    private void openShapePickerDialog() {
        // List of shapes
        String[] shapes = {"Rectangle", "Circle", "Rounded Rectangle"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Photo Shape")
                .setItems(shapes, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            selectedShape = "rectangle";
                            break;
                        case 1:
                            selectedShape = "circle";
                            break;
                        case 2:
                            selectedShape = "rounded_rectangle";
                            break;
                    }
                    Toast.makeText(MainActivity2.this, "Selected Shape: " + selectedShape, Toast.LENGTH_SHORT).show();
                    try {
                        updateImageShape(); // Update the image shape immediately in the UI
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .show();
    }

    // Method to update the image view shape
    private void updateImageShape() throws IOException {
        if (selectedImageUri != null) {
            // Get the selected image as Bitmap
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);

            // Apply the selected shape to the image view
            if (selectedShape.equals("circle")) {
                // Set ImageBitmap to ImageView
                photoImageView.setImageBitmap(bitmap);

                // Apply a circular shape to the ImageView
                photoImageView.setClipToOutline(true);
                photoImageView.setOutlineProvider(new ViewOutlineProvider() {
                    @Override
                    public void getOutline(View view, Outline outline) {
                        // Get the view's width and height (ensure it's square for circle)
                        int size = Math.max(view.getWidth(), view.getHeight());
                        // Clip the image into a full circle (centered)

                        outline.setOval(0, 0, size,size );
                    }
                });
            } else if (selectedShape.equals("rounded_rectangle")) {
                // Set ImageBitmap to ImageView
                photoImageView.setImageBitmap(bitmap);

                // Apply a rounded rectangle shape to the ImageView
                photoImageView.setClipToOutline(true);
                photoImageView.setOutlineProvider(new ViewOutlineProvider() {
                    @Override
                    public void getOutline(View view, Outline outline) {
                        // Get the view's width and height
                        int width = view.getWidth();
                        int height = view.getHeight();
                        // Ensure that the rounded rectangle fits the ImageView
                        outline.setRoundRect(0, 0, width, height,100f);  // 100f is the corner radius
                    }
                });
            } else {
                // Default rectangle shape (no clipping)
                photoImageView.setClipToOutline(false);
                photoImageView.setImageBitmap(bitmap);  // Set the image as it is
            }
        }


    }

    //Update the font size of the fields, PDF generation, or UI text
    private void updateFontSize(int newSize) {
        nameEditText.setTextSize(TypedValue.COMPLEX_UNIT_SP, newSize);
        emailEditText.setTextSize(TypedValue.COMPLEX_UNIT_SP, newSize);
        phoneEditText.setTextSize(TypedValue.COMPLEX_UNIT_SP, newSize);
        summaryEditText.setTextSize(TypedValue.COMPLEX_UNIT_SP, newSize);
        educationEditText.setTextSize(TypedValue.COMPLEX_UNIT_SP, newSize);
        exeperienceEditText.setTextSize(TypedValue.COMPLEX_UNIT_SP, newSize);
        projectsEditText.setTextSize(TypedValue.COMPLEX_UNIT_SP, newSize);
        skillsEditText.setTextSize(TypedValue.COMPLEX_UNIT_SP, newSize);
        achievementsEditText.setTextSize(TypedValue.COMPLEX_UNIT_SP, newSize);
    }

    //--------Reset all data
    private void resetForm() {
        nameEditText.setText("");
        emailEditText.setText("");
        phoneEditText.setText("");
        summaryEditText.setText("");
        educationEditText.setText("");
        exeperienceEditText.setText("");
        projectsEditText.setText("");
        skillsEditText.setText("");
        achievementsEditText.setText("");

        selectedImageUri = null;
        photoImageView.setImageResource(R.drawable.insertimageee);

        pdfBackgroundColor = BaseColor.WHITE;
        pdfTextColor = BaseColor.BLACK;

        selectedFontSize = 14;
        fontSizeSeekBar.setProgress(selectedFontSize - 1);

        photoImageView.setClipToOutline(false);
        photoImageView.setBackgroundResource(0);

        Toast.makeText(this, "Form cleared! You can enter new data.", Toast.LENGTH_SHORT).show();
    }

    //--------OpenColorPicker button
    private void openColorPicker(boolean isBackground) {
        new ColorPickerDialog.Builder(this)
                .setTitle(isBackground ? "Pick Background Color" : "Pick Text Color")
                .setPositiveButton("Select", (ColorEnvelopeListener) (envelope, fromUser) -> {
                    int color = envelope.getColor();
                    if (isBackground) {
                        pdfBackgroundColor = new BaseColor(
                                (color >> 16) & 0xFF, // Red
                                (color >> 8) & 0xFF,  // Green
                                color & 0xFF         // Blue
                        );
                        Toast.makeText(this, "Background color set!", Toast.LENGTH_SHORT).show();
                    } else {
                        pdfTextColor = new BaseColor(
                                (color >> 16) & 0xFF,
                                (color >> 8) & 0xFF,
                                color & 0xFF
                        );
                        Toast.makeText(this, "Text color set!", Toast.LENGTH_SHORT).show();
                    }

                })
                .setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.dismiss())
                .show();
    }
    private void openGallery() {
        // ✅ If no photo is selected, open gallery
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE);
    }

    private void requestStoragePermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_STORAGE_PERMISSION);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            photoImageView.setImageURI(selectedImageUri);  // ✅ ImageView me image show karna
        }
    }

    private Font getCustomFont(int size, int style, BaseColor color) {
        try {
            BaseFont customFont = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.EMBEDDED);
            return new Font(customFont, size, style, color);
        } catch (Exception e) {
            e.printStackTrace();
            return new Font(Font.FontFamily.HELVETICA, size, style, color); // Fallback
        }
    }
    private void generatePDF() {
        String name = nameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();
        String summary = summaryEditText.getText().toString().trim();
        String education = educationEditText.getText().toString().trim();
        String experience = exeperienceEditText.getText().toString().trim();
        String projects = projectsEditText.getText().toString().trim();
        String skills = skillsEditText.getText().toString().trim();
        String achievements = achievementsEditText.getText().toString().trim();

        Document document = new Document();
        try {
            File file = new File(getExternalFilesDir(null), "Resume_" + System.currentTimeMillis() + ".pdf");
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(file));
            document.open();
            // Draw a colored rectangle to simulate the background color
            writer.getDirectContentUnder().setColorFill(pdfBackgroundColor);
            writer.getDirectContentUnder().rectangle(0, 0, document.getPageSize().getWidth(), document.getPageSize().getHeight());
            writer.getDirectContentUnder().fill();

            Font headerFont = getCustomFont(selectedFontSize + 1, Font.BOLD, pdfTextColor);
            Font contentFont = getCustomFont(selectedFontSize - 2, Font.NORMAL, pdfTextColor);  // Slightly smaller for content
            Font sectionTitleFont = getCustomFont(selectedFontSize, Font.BOLD, pdfTextColor);  // Larger for section titles

            // ✅ Name, Email, Phone + Image ke liye check karega
            if (selectedImageUri == null) {
                // ✅ Agar Image Nahi Hai, To Name Center Me Rahega
                Paragraph nameParagraph = new Paragraph(name, headerFont);
                nameParagraph.setAlignment(Element.ALIGN_CENTER);
                document.add(nameParagraph);

                Paragraph contactParagraph = new Paragraph(email + "    " + phone, contentFont);
                contactParagraph.setAlignment(Element.ALIGN_CENTER);
                document.add(contactParagraph);
            } else {
                // ✅ Agar Image Hai, To Left Me Text Aur Right Me Image Rahegi
                PdfPTable table = new PdfPTable(2);
                table.setWidthPercentage(100);
                table.setSpacingBefore(10);

                PdfPCell textCell = new PdfPCell();
                textCell.setBorder(PdfPCell.NO_BORDER);
                textCell.addElement(new Paragraph(name, headerFont));
                textCell.addElement(new Paragraph(email, contentFont));
                textCell.addElement(new Paragraph(phone, contentFont));
                table.addCell(textCell);

                // ✅ Image ko PDF me Add Karna (Bigger Size & Adjusted Alignment)
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                Image image = Image.getInstance(stream.toByteArray());

                // Scale image to fit into a specified size
                image.scaleToFit(200, 120);  // Image size increased

                // Clip image based on the selected shape
                if (selectedShape.equals("circle")) {
                    // Use a circular mask
                    Bitmap circularBitmap = getCircularBitmap(bitmap);
                    ByteArrayOutputStream circularStream = new ByteArrayOutputStream();
                    circularBitmap.compress(Bitmap.CompressFormat.PNG, 100, circularStream);
                    Image circleImage = Image.getInstance(circularStream.toByteArray());
                    circleImage.scaleToFit(200, 120);

                    PdfPCell imageCell = new PdfPCell(circleImage);
                    imageCell.setBorder(PdfPCell.NO_BORDER);
                    imageCell.setPaddingLeft(140);  // Image shift to the left
                    imageCell.setHorizontalAlignment(Element.ALIGN_CENTER);  // Align the image properly
                    table.addCell(imageCell);

                } else if (selectedShape.equals("rounded_rectangle")) {
                    // Use rounded rectangle shape
                    Bitmap roundedBitmap = getRoundedBitmap(bitmap, 100f);  // Radius of 20px for rounded corners
                    ByteArrayOutputStream roundedStream = new ByteArrayOutputStream();
                    roundedBitmap.compress(Bitmap.CompressFormat.PNG, 100, roundedStream);
                    Image roundedImage = Image.getInstance(roundedStream.toByteArray());
                    roundedImage.scaleToFit(200, 120);

                    PdfPCell imageCell = new PdfPCell(roundedImage);
                    imageCell.setBorder(PdfPCell.NO_BORDER);
                    imageCell.setPaddingLeft(140);  // Image shift to the left
                    imageCell.setHorizontalAlignment(Element.ALIGN_CENTER);  // Align the image properly
                    table.addCell(imageCell);

                } else {
                    // Default rectangle (no clipping)
                    PdfPCell imageCell = new PdfPCell(image);
                    imageCell.setBorder(PdfPCell.NO_BORDER);
                    imageCell.setPaddingLeft(140);  // Image shift to the left
                    imageCell.setHorizontalAlignment(Element.ALIGN_CENTER);  // Align the image properly
                    table.addCell(imageCell);
                }

                document.add(table);
            }

            // Adding sections like summary, education, experience, etc.
            addSectionWithLine(document, "SUMMARY", sectionTitleFont, contentFont, summary);
            addSectionWithLine(document, "EDUCATION", sectionTitleFont, contentFont, education);
            addSectionWithLine(document, "EXPERIENCE", sectionTitleFont, contentFont, experience);
            addSectionWithLine(document, "PROJECTS", sectionTitleFont, contentFont, projects);
            addSectionWithLine(document, "SKILLS", sectionTitleFont, contentFont, skills);
            addSectionWithLine(document, "ACHIEVEMENTS", sectionTitleFont, contentFont, achievements);

            document.close();
            sharePDF(file);
        } catch (DocumentException | IOException e) {
            e.printStackTrace();
        }
    }

    // Method to create a circular bitmap
    private Bitmap getCircularBitmap(Bitmap bitmap) {
        int size = Math.min(bitmap.getWidth(), bitmap.getHeight());
        Bitmap output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(Color.parseColor("#FFFFFF"));

        canvas.drawCircle(size / 2, size / 2, size / 2, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, (size - bitmap.getWidth()) / 2, (size - bitmap.getHeight()) / 2, paint);

        return output;
    }

    // Method to create a rounded bitmap
    private Bitmap getRoundedBitmap(Bitmap bitmap, float radius) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(Color.parseColor("#FFFFFF"));

        RectF rectF = new RectF(0, 0, bitmap.getWidth(), bitmap.getHeight());
        canvas.drawRoundRect(rectF, radius, radius, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, 0, 0, paint);

        return output;
    }

    private void addSectionWithLine(Document document, String title, Font titleFont, Font contentFont, String content) throws DocumentException {
        if (content == null || content.trim().isEmpty()) {
            return; // ✅ Agar section empty hai to kuch add mat karo
        }

        // Add the title
        document.add(new Paragraph("\n"+title, titleFont));

        // Add a gap between the title and the line separator (for example, 5pt)
        document.add(new Paragraph(" ")); // Adding a newline (gap) between the title and the line

        // Add the dynamic line separator
        LineSeparator line = new LineSeparator();
        line.setOffset(-2f);  // Adjust the line's vertical position, if needed
        line.setLineColor(new BaseColor(0, 0, 0)); // Optional: Set line color (default black)

        // Add the line directly to the document
        document.add(line);

        // Add the content
        document.add(new Paragraph(content, contentFont));
    }


    //----------SharePdF
    private void sharePDF(File file) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("application/pdf");
        Uri uri = FileProvider.getUriForFile(this, "com.example.myapplication20.provider", file);
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(intent, "Share PDF"));
    }
}

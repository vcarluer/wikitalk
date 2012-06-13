package org.dragon.vince;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import android.app.Activity;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnUtteranceCompletedListener;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class WikitalkActivity extends Activity implements TextToSpeech.OnInitListener, OnUtteranceCompletedListener{

    private static final int VOICE_RECOGNITION_REQUEST_CODE = 1234;
    
	static final String WIKITALK = "wikitalk";
    private TextToSpeech mTts;    
    private ImageView mImage;
    private Button mImgNext;
    private Button mImgPrev;
    private TextView mTitle;
    private Handler mHandler;
    private TextView mLangPref;
    private Spinner mSupportedLanguageView;
	private String textToRead;
	private List<ImageInfo> images;
	private int imageCursor;
	private Status status;
	private Map<Integer, String> sentences;
	private int readCursor;
	private HashMap<String, String> hashAudio;
	private boolean reading;
	private RelativeLayout mainLayout;
	
	private Button mTmp;
	
	public WikitalkActivity() {
		this.sentences = new HashMap<Integer, String>();
		this.hashAudio = new HashMap<String, String>();
	}
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        this.status = Status.Ready;
        
        this.mTmp = (Button) findViewById(R.id.readIt);
        this.mTmp.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				search("Lyon");
			}
		});
        
     // Initialize text-to-speech. This is an asynchronous operation.
        // The OnInitListener (second argument) is called after initialization completes.
        if (this.mTts == null) {
        	mTts = new TextToSpeech(this,
                    this  //TextToSpeech.OnInitListener
                    );
        }

        this.mImage = (ImageView) findViewById(R.id.wikiImage);
        this.mImage.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				// showFullImage();
				if (status == Status.Ready) {
					if (mTts.isSpeaking()) {
						pauseRead();
					} else {
						resumeRead();
					}					
				}
			}
		});
        
        this.mainLayout = (RelativeLayout) findViewById(R.id.mainLayout);
        this.mainLayout.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				// showFullImage();
				if (status == Status.Ready) {
					if (mTts.isSpeaking()) {
						pauseRead();
					} else {
						resumeRead();
					}					
				}
			}
		});
        
        this.mImgPrev = (Button) findViewById(R.id.imgPrevious);
        this.mImgPrev.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				previousImage();
			}
		});
        
        this.mImgNext = (Button) findViewById(R.id.imgNext);
        this.mImgNext.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				nextImage();				
			}
		});
        
        // Get the intent, verify the action and get the query
        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
          String query = intent.getStringExtra(SearchManager.QUERY);
          search(query);
        }
        
        this.mTitle = (TextView) findViewById(R.id.txtTitle);
        this.mLangPref = (TextView) findViewById(R.id.language_preference);
        mSupportedLanguageView = (Spinner) findViewById(R.id.supported_languages);
        mHandler = new Handler();
     // Check to see if a recognition activity is present
        PackageManager pm = getPackageManager();
        List<ResolveInfo> activities = pm.queryIntentActivities(
                new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
        if (activities.size() != 0) {
            this.mTitle.setOnClickListener(new View.OnClickListener() {
				
				public void onClick(View v) {
					startVoiceRecognitionActivity();
				}
			});
        } else {
            this.mTitle.setEnabled(false);
            this.mTitle.setText("Recognizer not present");
        }
        
     // Most of the applications do not have to handle the voice settings. If the application
        // does not require a recognition in a specific language (i.e., different from the system
        // locale), the application does not need to read the voice settings.
        refreshVoiceSettings();
    }
    
    /**
     * Fire an intent to start the speech recognition activity.
     */
    private void startVoiceRecognitionActivity() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

        // Specify the calling package to identify your application
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getClass().getPackage().getName());

        // Display an hint to the user about what he should say.
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Rechercher dans Wikitalk");

        // Given an hint to the recognizer about what the user is going to say
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

        // Specify how many results you want to receive. The results will be sorted
        // where the first result is the one with higher confidence.
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5);

        // Specify the recognition language. This parameter has to be specified only if the
        // recognition has to be done in a specific language and not the default one (i.e., the
        // system locale). Most of the applications do not have to set this parameter.
        if (!mSupportedLanguageView.getSelectedItem().toString().equals("Default")) {
            String extraLang = mSupportedLanguageView.getSelectedItem().toString();
        	intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,
                    extraLang);
        	// other props
        	intent.putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE,
                    extraLang);
        }

        startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
    }

	public void onInit(int status) {
		// status can be either TextToSpeech.SUCCESS or TextToSpeech.ERROR.
        if (status == TextToSpeech.SUCCESS) {
                       int result = mTts.setLanguage(Locale.FRANCE);
           
            if (result == TextToSpeech.LANG_MISSING_DATA ||
                result == TextToSpeech.LANG_NOT_SUPPORTED) {
               // Lanuage data is missing or the language is not supported.
                Log.e(WIKITALK, "Language is not available.");
            } else {
                // Check the documentation for other possible result codes.
                // For example, the language may be available for the locale,
                // but not for the specified country and variant.
                // The TTS engine has been successfully initialized.
                // Allow the user to press the button for the app to speak again.
                // Greet the user.            	
            	this.mTts.setOnUtteranceCompletedListener(this);
            	this.hashAudio.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, WIKITALK);
            	sayHello();
            }
        } else {
            // Initialization failed.
            Log.e(WIKITALK, "Could not initialize TextToSpeech.");
        }
	}
	
	private static final Random RANDOM = new Random();
    private static final String[] HELLOS = {
      "Salut",
      "Le bouton ci-dessous récupérera les informations d'une recherche sur Albert Einstein",
      "Le bouton suivant lira l'article wikipedia"
      
    };
    int i =0;

    private void sayHello() {
        // Select a random hello.
        int helloLength = HELLOS.length;
        String hello = HELLOS[i];
        i++;
        if(i == helloLength) i =0;
        mTts.speak(hello,
            TextToSpeech.QUEUE_FLUSH,  // Drop allpending entries in the playback queue.
            null);
    }
	
	private void initTxt() {
		textToRead = "";		
	}
	
	public void addTextToRead(String line) {
		this.textToRead += line;
		Log.d(WIKITALK, line);
	}
	
	public void readText() {
		String[] splitSentence = this.textToRead.split("\\.");
		
		int idx = 0;
		for(String sentence : splitSentence) {			
			// Parse wikimedia tag here
			sentence = sentence.replaceAll("[\\[\\]]", "");
//			sentence = sentence.replaceAll("<br />", "");
//			sentence = sentence.replaceAll("<ref>", "");
//			sentence = sentence.replaceAll("</ref>", "");
//			sentence = sentence.replaceAll("/>", "");
//			sentence = sentence.replaceAll("<ref ", "");			
			if (sentence.trim().length() > 0 && 
					!sentence.contains("{") && 
					!sentence.contains("}") &&
					!sentence.contains("|") && 
					!sentence.contains("[") && 
					!sentence.contains("]") &&
					!sentence.contains("<") &&
					!sentence.contains(">") &&
					!sentence.contains("&") &&
					!sentence.contains("/") &&
					!sentence.contains("_")) {
				
				this.sentences.put(idx, sentence);
				
				idx++;
			}			
		}
		
		this.readCursor = 0;
		this.readAtPosition();
		
//		mTts.speak(this.textToRead,
//	            TextToSpeech.QUEUE_ADD,  // Drop allpending entries in the playback queue.
//	            null);
	}		
	
	public void setImages(List<ImageInfo> images) {
		this.images = images;
		this.showFirstImage();
	}
	
	private void showFirstImage() {
		this.imageCursor = 0;
		this.showImage();
	}

	private void showImage() {
		if (this.images.size() > this.imageCursor) {
			RetrieveImageTask retrieveImage = new RetrieveImageTask(this);
			retrieveImage.execute(this.images.get(this.imageCursor).thumbUrl);
		}
	}
	
	private void showFullImage() {
		if (this.images.size() > 0 && this.images.size() > this.imageCursor) {
			RetrieveImageTask retrieveImage = new RetrieveImageTask(this);
			retrieveImage.execute(this.images.get(this.imageCursor).url);
		}
	}
	
	public void showImage(Bitmap bitmap) {
		this.mImage.setImageBitmap(bitmap);
	}
	
	public void previousImage() {
		if (this.images != null) {
			this.imageCursor--;
			if (this.imageCursor < 0) {
				this.imageCursor = this.images.size() - 1;
			}
			
			this.showImage();
		}
	}
	
	public void nextImage() {
		if (this.images != null) {
			this.imageCursor++;
			if (this.imageCursor >= this.images.size()) {
				this.imageCursor = 0;
			}
			
			this.showImage();
		}			
	}
	
	public void search(String toSearch) {
		if (toSearch != null) {
			String searchStr = capitalizeFirstLetters(toSearch);
			this.mTitle.setText(searchStr);
			RetrievePageTask pageTask = new RetrievePageTask(this);		
			initTxt();
			pageTask.execute(searchStr);
		}
	}
	
	// voice reco
	 @Override
	    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	        if (requestCode == VOICE_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK) {
	            // Fill the list view with the strings the recognizer thought it could have heard
	            ArrayList<String> matches = data.getStringArrayListExtra(
	                    RecognizerIntent.EXTRA_RESULTS);
	            if (matches.size() > 0) {
	            	// Search first match
	            	this.search(matches.get(0));
	            }
	        }

	        super.onActivityResult(requestCode, resultCode, data);
	    }

	    private void refreshVoiceSettings() {
	        Log.i(WIKITALK, "Sending broadcast");
	        sendOrderedBroadcast(RecognizerIntent.getVoiceDetailsIntent(this), null,
	                new SupportedLanguageBroadcastReceiver(), null, Activity.RESULT_OK, null, null);
	    }

	    private void updateSupportedLanguages(List<String> languages) {
	        // We add "Default" at the beginning of the list to simulate default language.
	        languages.add(0, "Default");

	        SpinnerAdapter adapter = new ArrayAdapter<CharSequence>(this,
	                android.R.layout.simple_spinner_item, languages.toArray(
	                        new String[languages.size()]));
	        mSupportedLanguageView.setAdapter(adapter);
	    }

	    private void updateLanguagePreference(String language) {
	        this.mLangPref.setText(language);
	    }

	    /**
	     * Handles the response of the broadcast request about the recognizer supported languages.
	     *
	     * The receiver is required only if the application wants to do recognition in a specific
	     * language.
	     */
	    private class SupportedLanguageBroadcastReceiver extends BroadcastReceiver {

	        public void onReceive(Context context, final Intent intent) {
	            Log.i(WIKITALK, "Receiving broadcast " + intent);

	            final Bundle extra = getResultExtras(false);

	            if (getResultCode() != Activity.RESULT_OK) {
	                mHandler.post(new Runnable() {
	                    public void run() {
	                        showToast("Error code:" + getResultCode());
	                    }
	                });
	            }

	            if (extra == null) {
	                mHandler.post(new Runnable() {
	                    public void run() {
	                        showToast("No extra");
	                    }
	                });
	            }

	            if (extra.containsKey(RecognizerIntent.EXTRA_SUPPORTED_LANGUAGES)) {
	                mHandler.post(new Runnable() {

	                    public void run() {
	                        updateSupportedLanguages(extra.getStringArrayList(
	                                RecognizerIntent.EXTRA_SUPPORTED_LANGUAGES));
	                    }
	                });
	            }

	            if (extra.containsKey(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE)) {
	                mHandler.post(new Runnable() {

	                    public void run() {
	                        updateLanguagePreference(
	                                extra.getString(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE));
	                    }
	                });
	            }
	        }

	        private void showToast(String text) {
	            Toast.makeText(WikitalkActivity.this, text, 1000).show();
	        }
	    }
	    
	    public static String capitalizeFirstLetters ( String s ) {

	        for (int i = 0; i < s.length(); i++) {

	            if (i == 0) {
	                // Capitalize the first letter of the string.
	                s = String.format( "%s%s",
	                             Character.toUpperCase(s.charAt(0)),
	                             s.substring(1) );
	            }

	            // Is this character a non-letter or non-digit?  If so
	            // then this is probably a word boundary so let's capitalize
	            // the next character in the sequence.
	            if (!Character.isLetterOrDigit(s.charAt(i))) {
	                if (i + 1 < s.length()) {
	                    s = String.format( "%s%s%s",
	                                 s.subSequence(0, i+1),
	                                 Character.toUpperCase(s.charAt(i + 1)),
	                                 s.substring(i+2) );
	                }
	            }

	        }

	        return s;

	    }
	    
	    public void pauseRead() {	    	
	    	if (this.mTts.isSpeaking()) {
	    		this.mTts.stop();
	    	}
	    	
	    	this.reading = false;
	    }
	    
	    public void resumeRead() {
	    	this.readAtPosition();   		    
	    }
	    
	    private void readAtPosition() {	    	
	    	if (this.readCursor < this.sentences.size()) {
		    	String sentence = this.sentences.get(this.readCursor);
		    	this.reading = true;
	    		mTts.speak(sentence,
			            TextToSpeech.QUEUE_ADD,  // Drop allpending entries in the playback queue.
			            this.hashAudio);
	    	}
	    }
	    
	    public void readNext() {
	    	this.readCursor++;
	    	this.readAtPosition();
	    }

		public void onUtteranceCompleted(String utteranceId) {
			if (this.reading) {
				this.reading = false;
				this.readNext();
			}			
		}
}
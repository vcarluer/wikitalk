package org.dragon.vince;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnUtteranceCompletedListener;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher.ViewFactory;

public class WikitalkActivity extends Activity implements TextToSpeech.OnInitListener, OnUtteranceCompletedListener, ViewFactory  {

    private static final String DEFAULT_LANG = "Default";

	private static final String LINK_LABEL = "LinkLabel";

	private static final int VOICE_RECOGNITION_REQUEST_CODE = 1234;
    
	static final String WIKITALK = "wikitalk";
    private TextToSpeech mTts;    
    private ImageSwitcher mImage;
    private Button mImgNext;
    private Button mImgPrev;
    private TextView mTitle;
    private Handler mHandler;
    private Spinner mSupportedLanguageView;
	private String textToRead;
	private List<ImageInfo> images;
	private Map<Integer, List<ImageInfo>> imagesIndexed;
	private int imageCursor;
	private int imageTargetCursor;
	private Status status;
	private StatusImage statusImage;
	private Map<Integer, String> sentences;
	private int readCursor;
	private List<Link> links;
	private Map<Integer, List<Link>> linksIndexed;
	private int linkCursor;
	private int linkTargetCursor;
	private long linkShown;	
	
	private TextView mLinkInfo;
	private ImageView mLinkImage;
	
	private static Locale currentLang;
	
	private HashMap<String, String> hashAudio;
	private boolean reading;
	private RelativeLayout mainLayout;
		
	private String currentSentence;
	private Link currentLink;
	
	private TextView mImgInfo;
	private int textSize;	
	private String currentSearch;
	private long imageShown;
	private String langPref;
	
	private GestureDetector gestureDetector;
    private View.OnTouchListener gestureListener;
    
    private String[] splitSentence;
    
    private ProgressBar mSearchBar;
    private ProgressBar mProgressImage;
    
    private RelativeLayout main_info;
    private RelativeLayout main_noInfo;
    private ImageView main_Search;
	
	public WikitalkActivity() {
		this.sentences = new HashMap<Integer, String>();
		this.hashAudio = new HashMap<String, String>();
		this.linksIndexed = new HashMap<Integer, List<Link>>();
		this.imagesIndexed = new HashMap<Integer, List<ImageInfo>>(); 
		this.links = new ArrayList<Link>();
	}
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        this.status = Status.Ready;
        
     // Initialize text-to-speech. This is an asynchronous operation.
        // The OnInitListener (second argument) is called after initialization completes.
        if (this.mTts == null) {
        	mTts = new TextToSpeech(this,
                    this  //TextToSpeech.OnInitListener
                    );
        }

        this.mImage = (ImageSwitcher) findViewById(R.id.wikiImage);                
        this.mImage.setFactory(this);
        this.mImage.setInAnimation(AnimationUtils.loadAnimation(this,
                    android.R.anim.fade_in));
        this.mImage.setOutAnimation(AnimationUtils.loadAnimation(this,
                    android.R.anim.fade_out));
        
        
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
        
        this.mImgInfo = (TextView) findViewById(R.id.imgText);
        
        this.mainLayout = (RelativeLayout) findViewById(R.id.mainLayout);
        this.mainLayout.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				if (textToRead == null || textToRead == "") {
					startVoiceRecognitionActivity();
				} else {
					if (status == Status.Ready) {
						if (mTts.isSpeaking()) {
							pauseRead();
						} else {
							resumeRead();
						}					
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
        
        this.mTitle = (TextView) findViewById(R.id.txtTitle);                
        
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
        
        this.mLinkInfo = (TextView)	findViewById(R.id.linkInfo);
        this.mLinkInfo.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				if (currentLink != null) {
					search(currentLink.link);
				}
			}
		});
        
        this.mLinkImage = (ImageView) findViewById(R.id.linkImage);
        this.mLinkImage.setVisibility(View.GONE);
        this.mLinkImage.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				if (currentLink != null) {
					search(currentLink.link);
				}
			}
		});
        
     // Gesture detection
        gestureDetector = new GestureDetector(new MyGestureDetector(this));
        gestureListener = new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
            }
        };
        
        this.mImage.setOnTouchListener(this.gestureListener);
        this.mainLayout.setOnTouchListener(this.gestureListener);
        
        this.mSearchBar = (ProgressBar) findViewById(R.id.searchProgress);
        this.mProgressImage = (ProgressBar) findViewById(R.id.progressImage);
        
        this.main_info = (RelativeLayout) findViewById(R.id.main_info);
        this.main_noInfo = (RelativeLayout) findViewById(R.id.main_noinfo);
        this.main_Search = (ImageView) findViewById(R.id.main_search);
        this.main_Search.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				startVoiceRecognitionActivity();
			}
		});
        
        this.main_info.setVisibility(View.GONE);
        this.main_noInfo.setVisibility(View.VISIBLE);
        
        // Must be kept at end of method
        // Get the intent, verify the action and get the query
        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {        	        	
          String query = intent.getStringExtra(SearchManager.QUERY);
          query = capitalizeFirstLetters(query);
          
          String country = intent.getStringExtra("langCountry");
          String lang = intent.getStringExtra("langLang");
          if (country != null && lang != null) {
        	  currentLang = new Locale(country, lang);
          }          
          
//          Bundle appData = getIntent().getBundleExtra(SearchManager.APP_DATA);
//          if (appData != null) {
//        	  String country = appData.getString("langCountry");
//              String lang = appData.getString("langLang");
//              currentLang = new Locale(country, lang);
//          }
          
          search(query);
        }
    }
    
//    @Override
//    public void onSaveInstanceState(Bundle savedInstanceState) {
//      super.onSaveInstanceState(savedInstanceState);
//      // Save UI state changes to the savedInstanceState.
//      // This bundle will be passed to onCreate if the process is
//      // killed and restarted.
//      savedInstanceState.putString("langCountry", currentLang.getCountry());
//      savedInstanceState.putString("langLang", currentLang.getLanguage());      
//    }
//    
//    @Override
//    public void onRestoreInstanceState(Bundle savedInstanceState) {
//      super.onRestoreInstanceState(savedInstanceState);
//      // Restore UI state from the savedInstanceState.
//      // This bundle has also been passed to onCreate.
//      String country = savedInstanceState.getString("langCountry");
//      String lang = savedInstanceState.getString("langLang");
//      currentLang = new Locale(country, lang);
//    }
    
//    @Override
//    public boolean onSearchRequested() {
//         Bundle appData = new Bundle();
//         appData.putString("langCountry", currentLang.getCountry());
//         appData.putString("langLang", currentLang.getLanguage());
//         startSearch(null, false, appData, false);
//         return true;
//     }
    
    @Override
    public void startActivity(Intent intent) {
  
       if(Intent.ACTION_SEARCH.equals(intent.getAction())) {
    	   if (currentLang != null) {
    		   intent.putExtra("langCountry", currentLang.getCountry());
    	       intent.putExtra("langLang", currentLang.getLanguage());
    	   }          
       }
       super.startActivity(intent);
  
    }
    
    final Handler handler = new Handler() {
        public void  handleMessage(Message msg) {
            String info = msg.getData().getString(LINK_LABEL);        	
             if (info != null && info != "") {
            	 mLinkInfo.setText(info);
            	 mLinkImage.setVisibility(View.VISIBLE);
             }
        }
   };
   
   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
       MenuInflater inflater = getMenuInflater();
       inflater.inflate(R.menu.menu_main, menu);
       
       LayoutInflater mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE); 
       View sl = mInflater.inflate(R.layout.menu_spinner, null);       
       mSupportedLanguageView = (Spinner) sl.findViewById(R.id.supported_languages);
              
       menu.findItem(R.id.menu_lang).setActionView(mSupportedLanguageView);
//       mSupportedLanguageView = (Spinner) item.g

       // Get the SearchView and set the searchable configuration
       SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
       SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
       searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
       searchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default
       return true;
   }
   
   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
       switch (item.getItemId()) {
           case R.id.menu_speak:
        	   startVoiceRecognitionActivity();
        	   return true;
           default:
               return super.onOptionsItemSelected(item);
       }
   }
   
   final Handler handlerNextImage = new Handler() {
       public void  handleMessage(Message msg) {
            nextImage();
       }
  }; 
    
    /**
     * Fire an intent to start the speech recognition activity.
     */
    private void startVoiceRecognitionActivity() {
    	pauseRead();
    	
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
        if (!mSupportedLanguageView.getSelectedItem().toString().equals(DEFAULT_LANG)) {
            String extraLang = mSupportedLanguageView.getSelectedItem().toString();
        	intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,
                    extraLang);
        	// other props
        	intent.putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE,
                    extraLang);
        }

        startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
    }

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);
	}

	public void onInit(int status) {
		// status can be either TextToSpeech.SUCCESS or TextToSpeech.ERROR.
        if (status == TextToSpeech.SUCCESS) {                    	
           // Language set at search
            
            // Check the documentation for other possible result codes.
            // For example, the language may be available for the locale,
            // but not for the specified country and variant.
            // The TTS engine has been successfully initialized.
            // Allow the user to press the button for the app to speak again.
            // Greet the user.            	
        	this.mTts.setOnUtteranceCompletedListener(this);
        	this.hashAudio.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, WIKITALK);
            	sayHello();
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
    
	
	
    private void sayHello() {
    	int i = 0;
        // Select a random hello.
        int helloLength = HELLOS.length;
        String hello = HELLOS[i];
        i++;
        if(i == helloLength) i =0;
        mTts.speak(hello,
            TextToSpeech.QUEUE_FLUSH,  // Drop allpending entries in the playback queue.
            null);
    }
    
    private void initData() {
    	this.status = Status.Ready;
    	this.statusImage = StatusImage.Ready;
    	this.currentLink = null;
    	this.currentSearch = "";
    	this.currentSentence = "";
    	this.readCursor = 0;
    	this.imageCursor = -1;
    	this.imageTargetCursor = -1;
    	this.splitSentence = null;
    	this.imagesIndexed.clear();
    	this.linksIndexed.clear();
    	this.links.clear();
    	this.linkCursor = -1;
    	this.linkTargetCursor = -1;
    	this.sentences.clear();
    	this.textToRead = "";
    	this.textSize = -1;
    }
	
	private void initWidgets() {		
		this.mImage.setImageDrawable(null);
		this.mLinkInfo.setText("");
		this.mImgInfo.setText("");
		this.mLinkImage.setVisibility(View.GONE);		
	}
	
	public void addTextToRead(String line) {
		this.textToRead += line;
		Log.d(WIKITALK, line);
	}
	
	public void readText() {				
		this.mSearchBar.setVisibility(View.INVISIBLE);
		this.main_info.setVisibility(View.VISIBLE);
        this.main_noInfo.setVisibility(View.GONE);

		this.status = Status.Ready;
		this.sentences.clear();
		this.linksIndexed.clear();
		this.mTitle.setText(this.currentSearch);
		this.initLanguage();
		
		splitSentence = this.textToRead.split("\\. ");
		
		int idx = 0;
		for(String sentence : splitSentence) {
			this.currentSentence = new String(sentence);
			this.parseLinks(idx);
			this.parseRef(idx);
			// Replace if not numeric
			// sentence = sentence.replaceAll("-", " ");
			// Parse wikimedia tag here
//			sentence = sentence.replaceAll("[\\[\\]]", "");
//			sentence = sentence.replaceAll("<br />", "");
//			sentence = sentence.replaceAll("<ref>", "");
//			sentence = sentence.replaceAll("</ref>", "");
//			sentence = sentence.replaceAll("/>", "");
//			sentence = sentence.replaceAll("<ref ", "");			
			if (currentSentence.trim().length() > 0 && 
					!currentSentence.contains("{") && 
					!currentSentence.contains("}") &&
					!currentSentence.contains("|") && 
					!currentSentence.contains("[") && 
					!currentSentence.contains("]") &&
					!currentSentence.contains("<") &&
					!currentSentence.contains(">") &&
					!currentSentence.contains("&") &&
					!currentSentence.contains("/") &&
					!currentSentence.contains("_")) {
				
				this.sentences.put(idx, currentSentence);								
			}			
						
			idx++;
		}
		
		this.textSize = idx;
		
		this.readCursor = 0;
		this.readAtPosition();
		Toast.makeText(this, "Play", Toast.LENGTH_SHORT).show();
		
//		mTts.speak(this.textToRead,
//	            TextToSpeech.QUEUE_ADD,  // Drop allpending entries in the playback queue.
//	            null);
	}		
	
	private void initLanguage() {						
		int result = mTts.setLanguage(currentLang);
        
        if (result == TextToSpeech.LANG_MISSING_DATA ||
            result == TextToSpeech.LANG_NOT_SUPPORTED) {
           // Lanuage data is missing or the language is not supported.
            Log.e(WIKITALK, "Language is not available.");
        }
	}

	public void setImages(List<ImageInfo> fetchedImages) {
		this.endSearchImage();
		int idx = 0;
		List<ImageInfo> newImages = new ArrayList<ImageInfo>();
		if (this.splitSentence != null) {
			for(String sentence : this.splitSentence) {
				
				List<ImageInfo> iis = new ArrayList<ImageInfo>();
				
				for(ImageInfo ii : fetchedImages) {
					if (sentence.toUpperCase().contains(ii.name.toUpperCase())) {
						newImages.add(ii);
						ii.idx = newImages.size() - 1;
						iis.add(ii);					
					}
				}
				
				if (iis.size() > 0) {
					this.imagesIndexed.put(idx, iis);
				}
				
				idx++;
			}
			
			this.images = newImages;
		}
	}

	private void showImage() {
		if (this.images.size() > 0 && this.images.size() > this.imageCursor && this.statusImage == StatusImage.Ready) {
			this.statusImage = statusImage.Working;
			RetrieveImageTask retrieveImage = new RetrieveImageTask(this);
			ImageInfo ii = this.images.get(this.imageCursor);
			retrieveImage.execute(ii.thumbUrl);			
		}
	}

	private void showFullImage() {
		if (this.images.size() > 0 && this.images.size() > this.imageCursor && this.statusImage == StatusImage.Ready) {
			this.statusImage = statusImage.Working;
			RetrieveImageTask retrieveImage = new RetrieveImageTask(this);
			ImageInfo ii = this.images.get(this.imageCursor);
			retrieveImage.execute(ii.url);			
		}
	}
	
	public long getImageShown() {
		return this.imageShown;
	}
	
	public void showImage(Bitmap bitmap) {
		if (this.status == Status.Ready) {
			this.statusImage = statusImage.Ready;
			this.imageShown = System.currentTimeMillis();
			Drawable drawable = new BitmapDrawable(bitmap);
			this.mImage.setImageDrawable(drawable);
			if (this.images.size() > 0 && this.images.size() > this.imageCursor) {			
				ImageInfo ii = this.images.get(this.imageCursor);
				this.showImageInfo(ii);
			}
			
			Toast.makeText(this, String.valueOf(this.imageCursor + 1) + "/" + String.valueOf(this.images.size()), Toast.LENGTH_SHORT).show();
		}		
	}

	private void showImageInfo(ImageInfo imageInfo) {
		String info = "";
		if (imageInfo != null && imageInfo.name != null) {
			info = imageInfo.name;
			int pos = info.lastIndexOf(".");
			if (pos > -1) {
				info = info.substring(0, pos);
			}
		}
		
		info = Uri.decode(info);
		this.mImgInfo.setText(info);
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
		if (this.images != null && this.statusImage == StatusImage.Ready) {
			this.imageCursor++;
			if (this.imageCursor >= this.images.size()) {
				this.imageCursor = 0;
			}
			
			this.showImage();
		}			
	}
	
	public void search(String toSearch) {
		if (toSearch != null) {	        
			this.pauseRead();
			this.initData();
			initWidgets();
			this.mTitle.setText(toSearch);
			this.currentSearch = toSearch;
			this.setCurrentLang();
			
			RetrievePageTask pageTask = new RetrievePageTask(this);			
			this.mSearchBar.setVisibility(View.VISIBLE);
			this.main_info.setVisibility(View.GONE);
	        this.main_noInfo.setVisibility(View.VISIBLE);

			pageTask.execute(toSearch);
			this.status = Status.Working;
		}
	}
	
	private void setCurrentLang() {
		if (mSupportedLanguageView != null && mSupportedLanguageView.getSelectedItem() != null) {
			currentLang = Locale.US;
			String selected = mSupportedLanguageView.getSelectedItem().toString();
			if (!selected.equals(DEFAULT_LANG)) {            	            
				String[] locales = selected.split("-");
				if (locales.length == 2) {
					currentLang = new Locale(locales[0], locales[1]);
				}				
			}
		} else {
			if (currentLang == null) {
				currentLang = Locale.US;
			}
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
	            	String match = capitalizeFirstLetters(matches.get(0));
	            	this.search(match);
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
	        languages.add(0, DEFAULT_LANG);
	        int lgIdx = 0;
	        String toSearch = null;
	        
	        if (currentLang != null) {
	        	toSearch = currentLang.toString();
	        } else {
	        	if (this.langPref != null && this.langPref.length() > 0) {
		        	toSearch = this.langPref;
		        }
	        }	        
	        
	        if (toSearch != null) {
	        	int i = 0;
	        	for(String lang : languages) {
	        		if (langPref.toUpperCase().equals(lang.toUpperCase())) {
	        			lgIdx = i;
	        			break;
	        		}
	        		
	        		i++;
	        	}
	        }
	        
	        SpinnerAdapter adapter = new ArrayAdapter<CharSequence>(this,
	                android.R.layout.simple_spinner_item, languages.toArray(
	                        new String[languages.size()]));
	        if (mSupportedLanguageView != null) {
	        	mSupportedLanguageView.setAdapter(adapter);
		        mSupportedLanguageView.setSelection(lgIdx);
	        }	        

	        this.setCurrentLang();
	    }

	    private void updateLanguagePreference(String language) {
	        this.langPref = language;
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
	            
	            if (extra.containsKey(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE)) {
	                mHandler.post(new Runnable() {

	                    public void run() {
	                        updateLanguagePreference(
	                                extra.getString(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE));
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
	    	
	    	// Toast.makeText(this, "Pause", Toast.LENGTH_SHORT).show();
	    }
	    
	    public void resumeRead() {
	    	this.readAtPosition();   		
	    	// Toast.makeText(this, "Play", Toast.LENGTH_SHORT).show();
	    }
	    
	    private void readAtPosition() {
	    	if (this.readCursor < this.textSize) {
	    		if (this.linksIndexed != null && this.linksIndexed.size() > 0) {
	    			if (this.linksIndexed.containsKey(this.readCursor)) {
	    				List<Link> currentLinks = this.linksIndexed.get(this.readCursor);
		    			if (currentLinks.size() > 0) {
		    				Link l = currentLinks.get(currentLinks.size() - 1);
		    				this.linkTargetCursor = l.idx;
		    			}
	    			}
	    		}
	    		
	    		if (this.links != null && this.linkCursor < this.linkTargetCursor && System.currentTimeMillis() - this.linkShown > 5000) {
	    			this.linkCursor++;
	    			this.linkShown = System.currentTimeMillis();
	    			this.currentLink = links.get(this.linkCursor);
	    			Bundle bundle = new Bundle();
    				bundle.putString(LINK_LABEL, this.currentLink.label);
    				Message message = new Message();
    				message.setData(bundle);
    				this.handler.sendMessage(message);
	    		}	
	    		
	    		if (this.imagesIndexed != null && this.imagesIndexed.size() > 0) {
	    			if (this.imagesIndexed.containsKey(this.readCursor)) {
		    			// only first one
	    				List<ImageInfo> iis = this.imagesIndexed.get(this.readCursor);
	    				if (iis.size() > 0) {
	    					ImageInfo ii = iis.get(iis.size() - 1);
		    				this.imageTargetCursor = ii.idx;
	    				}	    						    			
		    		}
	    		}
	    		
	    		if (this.statusImage == StatusImage.Ready && this.images != null) {
		    		if ((this.imageCursor < this.imageTargetCursor && System.currentTimeMillis() - this.imageShown > 5000) || this.imageCursor == -1) {
		    			this.imageCursor++;
		    			this.showImage();
		    		}	    			    			
	    		}
	    		
		    	if (this.sentences.containsKey(this.readCursor)) {
		    		String sentence = this.sentences.get(this.readCursor);		    	
			    	this.reading = true;
		    		mTts.speak(sentence,
				            TextToSpeech.QUEUE_ADD,  // Drop allpending entries in the playback queue.
				            this.hashAudio);
		    	} else {
		    		this.readCursor++;		    		
		    		this.readAtPosition();		    				    	
		    	}
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
		
		private List<Link> GetSentenceLinks() {			
			List<String> linkFound = getTagValues(this.currentSentence, LINK_REGEX);
			List<Link> newLinks = new ArrayList<Link>();
			for(String linkStr : linkFound) {
				Link link = null;
				if (linkStr.contains("|")) {
					String[] linkStrSplit = linkStr.split("\\|");
					if (linkStrSplit.length == 2) {
						link = new Link();
						link.link = linkStrSplit[0];
						link.label = linkStrSplit[1];						
					}
				} else {
					link = new Link();
					link.label = linkStr;
					link.link = linkStr;
				}
				
				String replaceString = "";
				if (link != null) {
					newLinks.add(link);
					this.links.add(link);
					link.idx = this.links.size() - 1;
					replaceString = link.label;
				}
				
				this.currentSentence = this.currentSentence.replace("[[" + linkStr + "]]", replaceString);
			}

			return newLinks;
		}
		
		public void parseLinks(int idx) {
			this.linksIndexed.put(idx, this.GetSentenceLinks());
		}
		
		private void removeRef() {			
			List<String> linkFound = getTagValues(this.currentSentence, REF_REGEX);
			for(String linkStr : linkFound) {
				this.currentSentence = this.currentSentence.replace("<ref" + linkStr + "/ref>", "");
			}
			linkFound = getTagValues(this.currentSentence, REF2_REGEX);
			for(String linkStr : linkFound) {
				this.currentSentence = this.currentSentence.replace("<ref" + linkStr + "/>", "");
			}
		}
		
		public void parseRef(int idx) {
			this.removeRef();
		}
		
		private static final Pattern LINK_REGEX = Pattern.compile("\\[\\[(.+?)\\]\\]");
		private static final Pattern REF_REGEX = Pattern.compile("<ref(.+?)/ref>");
		private static final Pattern REF2_REGEX = Pattern.compile("<ref(.+?)/>");

		private static List<String> getTagValues(final String str, final Pattern regEx) {
		    final List<String> tagValues = new ArrayList<String>();
		    final Matcher matcher = regEx.matcher(str);
		    while (matcher.find()) {
		        tagValues.add(matcher.group(1));
		    }
		    return tagValues;
		}
		
		public void callNextImage() {
			Message message = new Message();
			this.handlerNextImage.sendMessage(message);
		}
		
		public String getLanguageLc() {
			return currentLang.getLanguage().toLowerCase();
		}
		
		public void swithReading() {
			if (status == Status.Ready) {
				if (mTts.isSpeaking()) {
					pauseRead();
				} else {
					resumeRead();
				}					
			}
		}

		public void beginSearchImages() {
			this.mProgressImage.setVisibility(View.VISIBLE);
		}
		
		public void endSearchImage() {
			this.mProgressImage.setVisibility(View.GONE);
		}
		
		public View makeView() {
            ImageView iView = new ImageView(this);
            iView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            iView.setLayoutParams(new
                        ImageSwitcher.LayoutParams(
                                    LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT));
            // iView.setBackgroundColor(0xFF000000);
            return iView;
      }
}
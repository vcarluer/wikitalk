package org.dragon.vince;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import android.app.Activity;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnUtteranceCompletedListener;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SubMenu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher.ViewFactory;

import com.google.ads.AdRequest;
import com.google.ads.AdView;

public class DanyActivity extends Activity implements TextToSpeech.OnInitListener, OnUtteranceCompletedListener, ViewFactory  {
	
	private static boolean DEBUG = false;

	private static final String LINK_LABEL = "LinkLabel";

	private static final int VOICE_RECOGNITION_REQUEST_CODE = 1234;
	private static final String SGS_VCR = "703A6FB6180B55E158105A7D9481857A";
	private static final String AdMobPublisherId = "a14fdb0fed6cda1";
	
	private static final int ACTION_LOCATION_CODE = 1;
	
	private static int GROUP_LANG = 1;
	private static int maxLinksInfo = 20;
	private static int maxLinksInfoGeo = 10;
    
	static final String DANY = "dany";
    private TextToSpeech mTts;    
    private boolean mTtsInited;
    private ImageSwitcher mImage;
    private Button mImgNext;
    private Button mImgPrev;
    private TextView mTitle;
    private Handler mHandler;
    private ImageRepository imageRepository;
	private int imageCursor;
	private int imageTargetCursor;
	private int readCursor;
	private int targetReadCursor;
	private int linkCursor;
	private int linkTargetCursor;
	private long linkShown;	
	
	private TextView mLinkInfo;
	private ImageView mLinkImage;
	private ProgressBar mProgressLoadImage;
	
	private static Locale currentLang;
	
	private HashMap<String, String> hashAudio;
	private boolean reading;
	private RelativeLayout mainLayout;
		
	private Link currentLink;
	
	private TextView mImgInfo;	
	private long imageShown;
	private Locale langPref;
	
	private GestureDetector gestureDetector;
    private View.OnTouchListener gestureListener;
    
    
    private ProgressBar mSearchBar;
    private ProgressBar mProgressImage;
    
    private RelativeLayout main_info;
    private RelativeLayout main_noInfo;
    private ImageView main_Search;
	
    private SeekBar mSeekText;
    
    private boolean resetImageCursor;
    private boolean resetLinkCursor;
    
    private ImageView mMediaInfo;
    
    private Animation fadeOutAnimation;
    private ImageView mSearchImage;
    private EditText mSearchText;
        
    private AdView adView;
    
    private RetrievePageTask retrievePageTask;
    private RetrieveImagesTask retrieveImagesTask;
    private RetrieveImageTask retrieveImageTask;
    private RetrievePoiTask retrievePoiTask;
    
    private Page page;
    
    private ImageView noResult;
    
 // Acquire a reference to the system Location Manager
    private LocationManager locationManager;
    private Location userLocation;
    private ImageView searchGeolocation;
    private LocationListener locationListener;
    
    private List<String> poiList;
    private RelativeLayout layoutInfoList;
    private ListView infoListView;
    private ImageView changeInfoView;
    private boolean showInfoList;
    private boolean mainInfoVisible;
	
	private List<String> infoList;	
	
	private LinearLayout bottomLayout;
    
	public DanyActivity() {
		this.hashAudio = new HashMap<String, String>();
		this.infoList = new ArrayList<String>();
	}
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        if (!DEBUG) {
        	
        }
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        
        this.initCountryCodeMapping();
        this.initLanguageCodeMapping();
        
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
				if (retrievePageTask == null) {
					if (mTts.isSpeaking()) {
						pauseRead();
					} else {
						resumeRead();
					}					
				}
			}
		});
        
//        this.mImage.setOnLongClickListener(new OnLongClickListener() {
//			public boolean onLongClick(View v) {
//				startVoiceRecognitionActivity();
//				return true;
//			}
//		});
        
        this.mProgressLoadImage = (ProgressBar) findViewById(R.id.progressLoadImage);
        
        this.mImgInfo = (TextView) findViewById(R.id.imgText);
        
        this.mainLayout = (RelativeLayout) findViewById(R.id.mainLayout);
        this.mainLayout.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				if (page == null) {
					startVoiceRecognitionActivity();
				} else {
					if (retrievePageTask == null) {
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
        
//        this.mImgPrev.setOnLongClickListener(new OnLongClickListener() {
//			public boolean onLongClick(View v) {
//				startVoiceRecognitionActivity();
//				return true;
//			}
//		});
        
        this.mImgNext = (Button) findViewById(R.id.imgNext);
        this.mImgNext.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				nextImage();				
			}
		});  
        
//        this.mImgNext.setOnLongClickListener(new OnLongClickListener() {
//			public boolean onLongClick(View v) {
//				startVoiceRecognitionActivity();
//				return true;
//			}
//		});
        
        this.mTitle = (TextView) findViewById(R.id.txtTitle); 
//        this.mTitle.setOnLongClickListener(new OnLongClickListener() {
//			
//			public boolean onLongClick(View v) {
//				startVoiceRecognitionActivity();
//				return true;
//			}
//		});
        
        mHandler = new Handler();
     // Check to see if a recognition activity is present
//        PackageManager pm = getPackageManager();
//        List<ResolveInfo> activities = pm.queryIntentActivities(
//                new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
//        if (activities.size() != 0) {
//            this.mTitle.setOnClickListener(new View.OnClickListener() {
//				
//				public void onClick(View v) {					
//					startVoiceRecognitionActivity();
//				}
//			});
//        } else {
//            this.mTitle.setEnabled(false);
//            this.mTitle.setText("Recognizer not present");
//        }        
        
        this.mLinkInfo = (TextView)	findViewById(R.id.linkInfo);
        this.mLinkInfo.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				if (currentLink != null) {
					search(currentLink.link);
				}
			}
		});
        
//        this.mLinkInfo.setOnLongClickListener(new OnLongClickListener() {			
//			public boolean onLongClick(View v) {
//				startVoiceRecognitionActivity();
//				return true;
//			}
//		});
        
        this.mLinkImage = (ImageView) findViewById(R.id.linkImage);
        this.mLinkImage.setVisibility(View.GONE);
        this.mLinkImage.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				if (currentLink != null) {
					search(currentLink.link);
				}
			}
		});
        
//        this.mLinkImage.setOnLongClickListener(new OnLongClickListener() {			
//			public boolean onLongClick(View v) {
//				startVoiceRecognitionActivity();
//				return true;
//			}
//		});
        
     // Gesture detection
        gestureDetector = new GestureDetector(new MyGestureDetector(this));
        gestureListener = new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
            }
        };
        
        this.mImage.setOnTouchListener(this.gestureListener);
        this.mainLayout.setOnTouchListener(this.gestureListener);
        this.mImgNext.setOnTouchListener(this.gestureListener);
        this.mImgPrev.setOnTouchListener(this.gestureListener);
        
        this.mSearchBar = (ProgressBar) findViewById(R.id.searchProgress);
        this.mProgressImage = (ProgressBar) findViewById(R.id.progressImage);
        
        this.main_info = (RelativeLayout) findViewById(R.id.main_info);
        this.main_noInfo = (RelativeLayout) findViewById(R.id.main_noinfo);
//        this.main_info.setOnLongClickListener(new OnLongClickListener() {			
//			public boolean onLongClick(View v) {
//				startVoiceRecognitionActivity();
//				return true;
//			}
//		});
//        this.main_noInfo.setOnLongClickListener(new OnLongClickListener() {			
//			public boolean onLongClick(View v) {
//				startVoiceRecognitionActivity();
//				return true;
//			}
//		});
        
        this.main_Search = (ImageView) findViewById(R.id.main_search);
        this.main_Search.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				startVoiceRecognitionActivity();
			}
		});
        
//        this.main_Search.setOnLongClickListener(new OnLongClickListener() {			
//			public boolean onLongClick(View v) {
//				startVoiceRecognitionActivity();
//				return true;
//			}
//		});
        
        this.mainInfoVisible = false;
        this.showInfoList = false;
        
        this.mSeekText = (SeekBar) findViewById(R.id.txtSeekBar);
        this.mSeekText.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			
        	public void onStopTrackingTouch(SeekBar seekBar) {
        		resetImageCursor = true;
        		resetLinkCursor = true;
        		pauseRead();
				targetReadCursor = seekBar.getProgress();
				readCursor = targetReadCursor - 1;
				resumeRead();
			}
			
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}
			
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				// TODO Auto-generated method stub
				
			}
		});
        
//        this.mSeekText.setOnLongClickListener(new OnLongClickListener() {			
//			public boolean onLongClick(View v) {
//				startVoiceRecognitionActivity();
//				return true;
//			}
//		});
        
        this.mMediaInfo = (ImageView) findViewById(R.id.media_readInfo);
        
        this.fadeOutAnimation = new AlphaAnimation(1.00f, 0.00f);
        this.fadeOutAnimation.setDuration(1000);
        this.fadeOutAnimation.setAnimationListener(new FadeOutAnimationListener(this.mMediaInfo));
        
        this.mSearchText = (EditText) findViewById(R.id.main_text_search);        
        this.mSearchText.setOnKeyListener(new  View.OnKeyListener() {			
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				// If the event is a key-down event on the "enter" button
		        if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
		            (keyCode == KeyEvent.KEYCODE_ENTER)) {
		          // Perform action on key press
		        	search(mSearchText.getText().toString());
		        	InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(mSearchText.getWindowToken(), 0);
		          return true;
		        }
		        return false;
			}
		});
        
//        this.mSearchText.setOnLongClickListener(new OnLongClickListener() {			
//			public boolean onLongClick(View v) {
//				startVoiceRecognitionActivity();
//				return true;
//			}
//		});
        
        this.mSearchImage = (ImageView) findViewById(R.id.main_img_search);
//        this.mSearchImage.setOnLongClickListener(new OnLongClickListener() {			
//			public boolean onLongClick(View v) {
//				startVoiceRecognitionActivity();
//				return true;
//			}
//		});
      
        this.adView = (AdView) findViewById(R.id.adView);
//        this.adView.setOnLongClickListener(new OnLongClickListener() {			
//			public boolean onLongClick(View v) {
//				startVoiceRecognitionActivity();
//				return true;
//			}			
//		});
        
        this.noResult = (ImageView) findViewById(R.id.no_result);
        this.noResult.setVisibility(View.GONE);
        
        this.locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
     // Define a listener that responds to location updates
        locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
              newLocation(location);
              tryStopLocation();
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {}

            public void onProviderEnabled(String provider) {}

            public void onProviderDisabled(String provider) {}
          };
        
        this.refreshLocation(false);
        this.searchGeolocation = (ImageView) findViewById(R.id.main_search_geolocate);
        this.searchGeolocation.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				geolocation();
			}
		});
//        
//        this.searchGeolocation.setOnLongClickListener(new OnLongClickListener() {
//			public boolean onLongClick(View v) {
//				startVoiceRecognitionActivity();
//				return true;
//			}
//		});
//        
//        View.OnLongClickListener longListener = new OnLongClickListener() {
//			public boolean onLongClick(View v) {
//				startVoiceRecognitionActivity();
//				return true;
//			}
//		};
		
        this.layoutInfoList = (RelativeLayout) findViewById(R.id.main_info_list);
//        this.layoutInfoList.setOnLongClickListener(longListener);
        this.layoutInfoList.setVisibility(View.GONE);
        this.infoListView = (ListView) findViewById(R.id.info_list);
//        this.infoListView.setOnLongClickListener(longListener); // does not work... Add for item
        this.infoListView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				 search(((TextView) view).getText().toString());
			}
		});
        this.changeInfoView = (ImageView) findViewById(R.id.imgChangeInfo);
//        this.changeInfoView.setOnLongClickListener(longListener);
        this.changeInfoView.setOnClickListener(new OnClickListener() {			
			public void onClick(View v) {
				changeInfo();
			}
		});
        
        this.bottomLayout = (LinearLayout) findViewById(R.id.bottom_footer);
//        this.bottomLayout.setOnLongClickListener(longListener);
        
        this.handleInfoListState();
        // Must be kept at end of method
        // Get the intent, verify the action and get the query
        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {        	        	
          String query = intent.getStringExtra(SearchManager.QUERY);
          
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
    
    private boolean refreshLocation(boolean showOption) {
    	this.startRequestLocation();
    	
    	if(!this.locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER ))
    	{
    		if(!this.locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
    			if (showOption) {
    				Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            	    startActivityForResult(myIntent, ACTION_LOCATION_CODE);
            	    return true;
    			} else {
    				this.userLocation = null;
    			}
    		} else {
    			// this.newLocation(this.locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER));
    		}
    	} else {
    		// this.newLocation(this.locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER));
    	}
    	
    	return false;
	}

	private AdRequest getRequest(String keyWord) {
    	AdRequest adRequest = new AdRequest();
    	if (keyWord != null && keyWord.length() > 0) {
    		adRequest.addKeyword(keyWord);
    	}
    	
    	 if (DEBUG) {
         	adRequest.addTestDevice(AdRequest.TEST_EMULATOR);               // Emulator
            adRequest.addTestDevice(SGS_VCR);                      // Test Android Device
         }        
    	
    	return adRequest;
    }
    
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
   
   private SubMenu submenuLang;
   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
       MenuInflater inflater = getMenuInflater();
       inflater.inflate(R.menu.menu_main, menu);
       this.submenuLang = menu.findItem(R.id.item_langs).getSubMenu();
       this.updateLangList();       
       
       // Get the SearchView and set the searchable configuration
       SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
       SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
       searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
       searchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default
       return true;
   }
   
   private void updateLangList() {
	   if (this.submenuLang != null && this.submenuLang.size() == 0 && this.filteredLanguages != null) {
		   Collections.sort(this.filteredLanguages, new LocaleComparer());
		   
    	   for(int i = 0; i < this.filteredLanguages.size(); i++) {
    		   submenuLang.add(GROUP_LANG, i, Menu.NONE, this.filteredLanguages.get(i).getDisplayName());
    	   }
       }
   }

@Override
   public boolean onOptionsItemSelected(MenuItem item) {
		if (!this.mTtsInited) {
			Toast.makeText(this, getString(R.string.waitinit), Toast.LENGTH_SHORT).show();
			return true;
		}	
		
		if (item.getGroupId() == GROUP_LANG) {
			this.langIdx = item.getItemId();
			this.setCurrentLang();
			return true;
		} else {
			switch (item.getItemId()) {
	           case R.id.menu_speak:
	        	   startVoiceRecognitionActivity();
	        	   return true;
	           case R.id.menu_geosearch:
	        	   geolocation();
	        	   return true;
	           default:
	               return super.onOptionsItemSelected(item);
	       }
		}       
   }
  
  final Handler handlerProgressImage = new Handler() {
	  public void handleMessage(Message msg) {
		  mProgressLoadImage.setVisibility(View.VISIBLE);
	  }
  };
    
    /**
     * Fire an intent to start the speech recognition activity.
     */
    private void startVoiceRecognitionActivity() {
    	if (!this.mTtsInited) {
			Toast.makeText(this, getString(R.string.waitinit), Toast.LENGTH_SHORT).show();
			return;
		}
    	
    	pauseRead();
    	this.cancelGeolocation();
    	
    	Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

        // Specify the calling package to identify your application
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getClass().getPackage().getName());

        // Display an hint to the user about what he should say.
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.voice_prompt));

        // Given an hint to the recognizer about what the user is going to say
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

        // Specify how many results you want to receive. The results will be sorted
        // where the first result is the one with higher confidence.
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5);

        // Specify the recognition language. This parameter has to be specified only if the
        // recognition has to be done in a specific language and not the default one (i.e., the
        // system locale). Most of the applications do not have to set this parameter.
        this.setCurrentLang();
        if (currentLang != Locale.US) {
            String extraLang = getLocaleString((Locale) currentLang);
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
	
	private Map<String, Locale> localeCountryMap;

	private void initCountryCodeMapping() {
		String[] countries = Locale.getISOCountries();
		localeCountryMap = new HashMap<String, Locale>(countries.length);
		for (String country : countries) {
			Locale locale = new Locale("", country);
			localeCountryMap.put(locale.getISO3Country().toUpperCase(), locale);
		}
	}
	
	private String iso3CountryCodeToIso2CountryCode(String iso3CountryCode) {
		return localeCountryMap.get(iso3CountryCode).getCountry();
	}
	
	private String iso2CountryCodeToIso3CountryCode(String iso2CountryCode){
		Locale locale = new Locale("", iso2CountryCode);
		return locale.getISO3Country();
	}
	
	private Map<String, Locale> localeLanguageMap;

	private void initLanguageCodeMapping() {
		String[] languages = Locale.getISOLanguages();
		localeLanguageMap = new HashMap<String, Locale>(languages.length);
		for (String language : languages) {
		    Locale locale = new Locale(language);
		    localeLanguageMap.put(locale.getISO3Language(), locale);
		}
	}
	
	private String iso3LanguageCodeToIso2LanguageCode(String iso3LanguageCode) {
		return localeLanguageMap.get(iso3LanguageCode).getLanguage();
	}
	
	private String iso2LanguageCodeToIso3LanguageCode(String iso2LanguageCode){
		Locale locale = new Locale("", iso2LanguageCode);
		return locale.getISO3Language();
	}

	public void onInit(int status) {
		// status can be either TextToSpeech.SUCCESS or TextToSpeech.ERROR.
        if (status == TextToSpeech.SUCCESS) {                    
        	mTtsInited = true;
        	this.setLangPref(mTts.getLanguage());
           // Language set at search
            
            // Check the documentation for other possible result codes.
            // For example, the language may be available for the locale,
            // but not for the specified country and variant.
            // The TTS engine has been successfully initialized.
            // Allow the user to press the button for the app to speak again.
            // Greet the user.            	
        	this.mTts.setOnUtteranceCompletedListener(this);
        	this.hashAudio.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, DANY);
        	
            // Most of the applications do not have to handle the voice settings. If the application
            // does not require a recognition in a specific language (i.e., different from the system
            // locale), the application does not need to read the voice settings.
            refreshVoiceSettings();
        } else {
        	mTtsInited = false;
            // Initialization failed.
            Loge(DANY, "Could not initialize TextToSpeech.");
        }
	}    
	    
    private void initData() {
    	this.currentLink = null;
    	this.readCursor = -1;
    	this.targetReadCursor = -1;
    	this.imageCursor = -1;
    	this.imageTargetCursor = -1;
    	this.imageRepository = null;
    	this.linkCursor = -1;
    	this.linkTargetCursor = -1;
    	this.resetImageCursor = false;
    	this.resetLinkCursor = false;
    	this.retrieveImagesTask = null;
    	this.retrieveImageTask = null;
    	this.retrievePageTask = null;
    	this.page = null;
    	this.poiList = null;
    	this.showInfoList = false;
    	this.mainInfoVisible = false;
		this.infoList.clear();
    }
	
	private void initWidgets() {		
		this.mImage.setImageResource(R.drawable.dummy);
		this.mLinkInfo.setText("");
		this.mImgInfo.setText("");
		this.mLinkImage.setVisibility(View.GONE);		
		this.mProgressImage.setVisibility(View.GONE);
		this.mProgressLoadImage.setVisibility(View.GONE);
		this.mImgPrev.setVisibility(View.GONE);
		this.mImgNext.setVisibility(View.GONE);
		this.mSeekText.setVisibility(View.GONE);
		this.mMediaInfo.setVisibility(View.GONE);
		this.layoutInfoList.setVisibility(View.GONE);
		this.mSearchBar.setVisibility(View.VISIBLE);
		this.main_info.setVisibility(View.GONE);
        this.main_noInfo.setVisibility(View.VISIBLE);
        this.layoutInfoList.setVisibility(View.GONE);
        this.changeInfoView.setVisibility(View.GONE);
	}
	
	public static void Logd(String tag, String msg) {
		if (DEBUG) {
			Log.d(tag, msg);
		}
	}
	
	public static void Logi(String tag, String msg) {
		if (DEBUG) {
			Log.i(tag, msg);
		}
	}
	
	public static void Loge(String tag, String msg) {
		if (DEBUG) {
			Log.e(tag, msg);
		}
	}
	
	public void stopSearchBar() {
		this.mSearchBar.setVisibility(View.INVISIBLE);
		this.retrievePageTask = null;
	}
	
	public void readText(Page page) {
		this.mainInfoVisible = true;
		this.handleInfoListState();
        if (page != null)  {
        	this.page = page;
    		this.initLanguage();
    		this.mSeekText.setVisibility(View.VISIBLE);
    		if (this.page.splitSentence != null) {
    			this.mSeekText.setMax(this.page.splitSentence.length - 1);
    		} else {
    			this.mSeekText.setMax(0);
    		}
    		
    		this.mSeekText.setProgress(0);
    		    		 
    		this.targetReadCursor = 0;
    		this.resumeRead();
        }
	}

	private void initLanguage() {						
		int result = mTts.setLanguage(currentLang);
        
        if (result == TextToSpeech.LANG_MISSING_DATA ||
            result == TextToSpeech.LANG_NOT_SUPPORTED) {
           // Lanuage data is missing or the language is not supported.
            Loge(DANY, "Language is not available.");
            Toast.makeText(this, getString(R.string.language_not_available), Toast.LENGTH_SHORT).show();
        }
	}

	public void setImages(ImageRepository fetchedImages) {
		this.retrieveImagesTask = null;
		this.imageRepository = fetchedImages;
		this.endSearchImage();
	}

	private void showImage() {
		if (this.imageRepository != null && this.imageRepository.images.size() > 0 && 
				this.imageRepository.images.size() > this.imageCursor && this.retrieveImagesTask == null) {
			ImageInfo ii = this.imageRepository.images.get(this.imageCursor);
			this.handlerProgressImage.sendMessage(new Message());
			this.getNewRetrieveImage().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, ii);			
		}
	}
	
	public long getImageShown() {
		return this.imageShown;
	}
	
	public void showImage(ImageInfo imageInfo) {
		this.retrieveImageTask = null;
		this.mProgressLoadImage.setVisibility(View.GONE);
		if (this.retrieveImagesTask == null) {						
			if (imageInfo != null && imageInfo.bitmap != null) {
				if (this.imageRepository != null && this.imageRepository.images.size() > 0) {
					this.imageShown = System.currentTimeMillis();
					Drawable drawable = new BitmapDrawable(imageInfo.bitmap);
					this.mImage.setImageDrawable(drawable);
					if (this.imageRepository.images.size() > 0 && this.imageRepository.images.size() > this.imageCursor) {								
						this.showImageInfo(imageInfo.name);
					}
					
					Toast.makeText(this, String.valueOf(this.imageCursor + 1) + "/" + String.valueOf(this.imageRepository.images.size()), Toast.LENGTH_SHORT).show();
				}				
			}			
		}		
	}

	private void showImageInfo(String imageName) {
		String info = "";
		if (imageName != null && imageName != null) {
			info = imageName;
			int pos = info.lastIndexOf(".");
			if (pos > -1) {
				info = info.substring(0, pos);
			}
		}
		
		info = Uri.decode(info);
		this.mImgInfo.setText(info);
		// For accessibility
		this.mImage.setContentDescription(info);
	}
	
	public void previousImage() {
		if (this.imageRepository != null && this.imageRepository.images != null && this.retrieveImageTask == null) {
			this.imageCursor--;
			if (this.imageCursor < 0) {
				this.imageCursor = this.imageRepository.images.size() - 1;
			}
			
			this.resetImageCursor = false;
			this.showImage();
		}
	}
	
	public void nextImage() {
		if (this.imageRepository != null && this.imageRepository.images != null && this.retrieveImageTask == null) {
			this.imageCursor++;
			if (this.imageCursor >= this.imageRepository.images.size()) {
				this.imageCursor = 0;
			}
			
			this.resetImageCursor = false;
			this.showImage();
		}			
	}
	
	public void geolocation() {
		this.geolocation(true);
	}
	
	public void geolocation(boolean showOption) {
		if (!this.mTtsInited) {
			Toast.makeText(this, getString(R.string.waitinit), Toast.LENGTH_SHORT).show();
			return;
		}
		
		boolean optionShown = this.refreshLocation(showOption);
		if(!this.locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
			Toast.makeText(this, getString(R.string.enable_network), Toast.LENGTH_LONG).show();
		}
		
		if(!this.locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER )) {
			Toast.makeText(this, getString(R.string.enable_gps), Toast.LENGTH_LONG).show();
		}

		if (!optionShown && this.userLocation != null) {
			this.cancelAllAsync();
			this.pauseRead();
			this.initData();
			this.initWidgets();
			this.mTitle.setText(getString(R.string.search_geolocate));
			this.setCurrentLang();
						
			this.retrievePoiTask = new RetrievePoiTask(this);
			this.retrievePoiTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, this.userLocation);					
		}
	}
	
	public void search(String toSearch) {
		SearchParam search = new SearchParam();
		search.searchWord = toSearch;
		search.isStrictSearch = false;
		this.search(search);
	}
	
	public void search(SearchParam toSearch) {
		if (!this.mTtsInited) {
			Toast.makeText(this, getString(R.string.waitinit), Toast.LENGTH_SHORT).show();
			return;
		}
		
		if (toSearch != null) {
			this.cancelAllAsync();
			
			this.adView.loadAd(this.getRequest(toSearch.searchWord));
			this.pauseRead();
			this.initData();
			this.initWidgets();
			this.mTitle.setText(toSearch.searchWord);
			this.setCurrentLang();
			
			this.retrievePageTask = new RetrievePageTask(this);			
			

			this.retrievePageTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, toSearch);
		}
	}
	
	private void cancelAllAsync() {
		this.cancelGeolocation();
		
		if (this.retrievePageTask != null) {
			this.retrievePageTask.cancel(true);
		}
		
		this.cancelImagesAsync();
	}
	
	private void cancelGeolocation() {
		if (this.retrievePoiTask != null) {
			this.retrievePoiTask.cancel(true);
		}
	}

	public void cancelImagesAsync() {
		if (this.retrieveImagesTask != null) {
			this.retrieveImagesTask.cancel(true);
		}
		
		this.cancelImageAsync();
	}
	
	public void cancelImageAsync() {
		if (this.retrieveImageTask != null) {
			this.retrieveImageTask.cancel(true);
		}
	}
	
	public RetrieveImagesTask getNewRetrieveImages() {
		this.cancelImagesAsync();
		this.retrieveImagesTask = new RetrieveImagesTask(this);
		return this.retrieveImagesTask;
	}
	
	public RetrieveImageTask getNewRetrieveImage() {
		this.cancelImageAsync();
		this.retrieveImageTask = new RetrieveImageTask(this);
		return this.retrieveImageTask;
	}
	
	private void setLangPref(Locale lang) {
		if (langPref == null && lang != null) {
			langPref = lang;
		}
	}
	
	private void setCurrentLang() {
		if (currentLang == null) {
			if (langPref == null) {
				currentLang = Locale.US;
			} else {
				currentLang = langPref;
			}			
		}		
		
		if (this.langIdx > -1 && this.filteredLanguages != null) {			
			if (this.langIdx < this.filteredLanguages.size()) {
				currentLang = this.filteredLanguages.get(this.langIdx);
			}
		}
		
		String country = currentLang.getCountry();
		String language = currentLang.getLanguage();
		boolean recreate = false;
		if (country.length() > 2) {
			country = this.iso3CountryCodeToIso2CountryCode(country);
			recreate = true;
		}
		
		if (language.length() > 2) {
			language = this.iso3LanguageCodeToIso2LanguageCode(language);
			recreate = true;
		}
		
		if (recreate) {
			currentLang = new Locale(language, country);
		}
	}
	
	public static String getLocaleString(Locale locale)
	{
		if (locale == null) return null;
		
		if (locale.getCountry() != null && locale.getCountry() != "") {
			return locale.getLanguage() + "-" + locale.getCountry();
		} else {
			return locale.getLanguage();
		}		
	}
	
	private Locale createLocale(String str) {
		Locale locale = null;
		int pos = str.indexOf("-");
		if (pos > -1) {
			String lang = str.substring(0, pos);
			String coun = str.substring(pos + 1);
			locale = new Locale(lang, coun);
		}
		
		return locale;
	}

	// voice reco
	 @Override
	    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		 super.onActivityResult(requestCode, resultCode, data);   
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
	        
	        if (requestCode == ACTION_LOCATION_CODE) {
	        	this.geolocation(false);
	        }
	    }

	    private void refreshVoiceSettings() {
	        Logi(DANY, "Sending broadcast");
	        Intent detailsIntent =  new Intent(RecognizerIntent.ACTION_GET_LANGUAGE_DETAILS);
//	        sendOrderedBroadcast(RecognizerIntent.getVoiceDetailsIntent(this), null,
//	                new SupportedLanguageBroadcastReceiver(), null, Activity.RESULT_OK, null, null);
	        sendOrderedBroadcast(detailsIntent, null,
	                new SupportedLanguageBroadcastReceiver(), null, Activity.RESULT_OK, null, null);
	    }

	    private void updateSupportedLanguages(List<String> languages) {	        	        
	        this.setCurrentLang();
			// Remove unsupported tts languages	        
			filteredLanguages = new ArrayList<Locale>();
	        for(String lang : languages) {				
	        	if (this.mTts != null) {
					Locale locale = this.createLocale(lang);
					if (locale != null) {
						int langSupported = this.mTts.isLanguageAvailable(locale);
						if (langSupported != TextToSpeech.LANG_MISSING_DATA && langSupported != TextToSpeech.LANG_NOT_SUPPORTED) {
							filteredLanguages.add(locale);
						}
					}					
				}
	        }

	        this.updateLangList();
	    }
	    
	    private List<Locale> filteredLanguages;
	    private int langIdx = -1;

	    private void updateLanguagePreference(String language) {
	         this.setLangPref(createLocale(language));
	    }

	    /**
	     * Handles the response of the broadcast request about the recognizer supported languages.
	     *
	     * The receiver is required only if the application wants to do recognition in a specific
	     * language.
	     */
	    private class SupportedLanguageBroadcastReceiver extends BroadcastReceiver {

	        public void onReceive(Context context, final Intent intent) {
	            Logi(DANY, "Receiving broadcast " + intent);

	            final Bundle extra = getResultExtras(false);

	            if (getResultCode() != Activity.RESULT_OK) {
	                mHandler.post(new Runnable() {
	                    public void run() {
	                        // showToast("Error code:" + getResultCode());
	                    }
	                });
	            }

	            if (extra == null) {
	                mHandler.post(new Runnable() {
	                    public void run() {
	                        // showToast("No extra");
	                    }
	                });
	                
	                return;
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
	    	this.mMediaInfo.setImageResource(android.R.drawable.ic_media_pause);
	    	this.mMediaInfo.setVisibility(View.VISIBLE);
	    	this.mMediaInfo.setAlpha(0.5f);
	    	this.reading = false;
	    	this.readCursor = this.targetReadCursor - 1;
	    	if (this.mTts.isSpeaking()) {
	    		this.mTts.stop();
	    	}
	    	
	    	// Toast.makeText(this, "Pause", Toast.LENGTH_SHORT).show();
	    }
	    
	    public void resumeRead() {
	    	this.showReadImage();
	    	this.reading = true;	    	
	    	this.step();
	    }
	    
	    public void showReadImage() {
	    	this.mMediaInfo.setAlpha(1.0f);
	    	this.mMediaInfo.setImageResource(android.R.drawable.ic_media_play);
	    	this.mMediaInfo.setVisibility(View.VISIBLE);
	    	this.mMediaInfo.startAnimation(this.fadeOutAnimation);
		}

		private void readAtPosition() {
			// todo: last test can remove last links and images...
			int nextStep = 500;
			if (this.page != null && this.page.splitSentence != null) {
				if (this.readCursor < this.page.splitSentence.length) {									
					// Start request 7 sentences before end
					if (this.page.splitSentence.length - this.readCursor < 7) {
						this.startRequestLocation();
					}
					
		    		this.mSeekText.setProgress(this.readCursor);
		    		
		    		int firstListIdx = 0;
		    		if (this.page.linksIndexed != null && this.page.linksIndexed.size() > 0) {
		    			if (this.page.linksIndexed.containsKey(this.readCursor)) {
		    				List<Link> currentLinks = this.page.linksIndexed.get(this.readCursor);
			    			if (currentLinks.size() > 0) {
			    				Link l = currentLinks.get(currentLinks.size() - 1);
			    				this.linkTargetCursor = l.idx;
			    				l = currentLinks.get(0);
			    				firstListIdx = l.idx;
			    			}
		    			}
		    		}
		    			    		
		    		if (this.page.links != null) {
		    			boolean needSend = false;
		    			if (this.resetLinkCursor && this.page.linksIndexed.containsKey(this.targetReadCursor)) {
		    				List<Link> targetLinks = this.page.linksIndexed.get(this.targetReadCursor);
			    			if (targetLinks.size() > 0) {
			    				Link l = targetLinks.get(0);
			    				firstListIdx = l.idx;		    						    			
			    				if (this.linkCursor != firstListIdx) {
			    					this.linkCursor = firstListIdx;
			    					needSend = true;
			    				}
			    				
			    				this.resetLinkCursor = false;
			    			}	    					    			
		    			} else {	    				
		    				if (this.linkCursor < this.linkTargetCursor && System.currentTimeMillis() - this.linkShown > 3000) {
		    					if (this.linkTargetCursor - this.linkCursor > 10) {
		    						this.linkCursor = firstListIdx;
		    					} else {
		    						this.linkCursor++;
		    					}
		    					
		    	    			needSend = true;
		    				}
		    			}	    				    			
		    			
		    			if (needSend && this.page != null && this.page.links.size() > this.linkCursor) {
		    				this.mLinkImage.setVisibility(View.VISIBLE);
		    				this.mLinkInfo.setVisibility(View.VISIBLE);
		    				this.linkShown = System.currentTimeMillis();
	    	    			this.currentLink = this.page.links.get(this.linkCursor);
	    	    			Bundle bundle = new Bundle();
	        				bundle.putString(LINK_LABEL, this.currentLink.label);
	        				Message message = new Message();
	        				message.setData(bundle);
	        				this.handler.sendMessage(message);
		    			}
		    		}	
		    			    		
		    		if (this.imageRepository != null && this.imageRepository.imagesIndexed != null && this.imageRepository.imagesIndexed.size() > 0) {
		    			if (this.imageRepository.imagesIndexed.containsKey(this.readCursor)) {
			    			// only first one
		    				List<ImageInfo> iis = this.imageRepository.imagesIndexed.get(this.readCursor);
		    				if (iis.size() > 0) {
		    					ImageInfo ii = iis.get(iis.size() - 1);
			    				this.imageTargetCursor = ii.idx;
			    				ii = iis.get(0);
			    				firstListIdx = ii.idx;
		    				}	    						    			
			    		}
		    		}
		    		
		    		if (this.retrieveImageTask == null && this.imageRepository != null && 
		    				this.imageRepository.images != null && this.imageRepository.images.size() > 0) {
		    			boolean needShow = false;
		    			if (this.resetImageCursor && this.imageRepository.imagesIndexed.containsKey(this.targetReadCursor)) {
		    				List<ImageInfo> iis = this.imageRepository.imagesIndexed.get(this.targetReadCursor);
		    				if (iis.size() > 0) {
		    					ImageInfo ii = iis.get(0);
			    				firstListIdx = ii.idx;		    					    					 
			    				if (this.imageCursor != firstListIdx) {
			    					this.imageCursor = firstListIdx;
			    					needShow = true;
			    				}
			    				
			    				this.resetImageCursor = false;
		    				}	    					    				
		    			} else {
		    				if ((this.imageCursor < this.imageTargetCursor && System.currentTimeMillis() - this.imageShown > 5000) || this.imageCursor == -1) {
				    			if (this.imageTargetCursor - this.imageCursor > 10) {
				    				this.imageCursor = firstListIdx;
				    			} else {
				    				this.imageCursor++;
				    			}
		    					
				    			needShow = true;
				    		}
		    			}
		    				    			
		    			if (needShow) {
		    				this.showImage();
		    			}
		    		}
		    		
		    		if (this.readCursor < this.targetReadCursor)
		    		{
		    			this.readCursor = this.targetReadCursor;
		    			if (this.page.sentences.containsKey(this.readCursor)) {
				    		String sentence = this.page.sentences.get(this.readCursor);		    	
					    	this.reading = true;
				    		mTts.speak(sentence,
						            TextToSpeech.QUEUE_ADD,  // Drop allpending entries in the playback queue.
						            this.hashAudio);
				    	} else {
				    		this.readNext();
				    		nextStep = 0;
				    	}
		    		}
					
					this.stepNext(nextStep);
		    	} else {
		    		this.mLinkImage.setVisibility(View.GONE);
    				this.mLinkInfo.setVisibility(View.GONE);
		    		this.showInfo();
		    	}
			}	    		    	
	    }
	    
	    public void readNext() {
	    	this.targetReadCursor++;
	    }

		public void onUtteranceCompleted(String utteranceId) {
			if (this.reading) {
				this.readNext();
			}			
		}
				
		public String getWikipediaLanguageLc() {
			String lg = currentLang.getLanguage().toLowerCase();
//			if (lg.length() > 2) {
//				lg = this.iso3LanguageCodeToIso2LanguageCode(lg);
//			}
			
			// Chinese
			if (lg.equals("cmn") || lg.equals("yue")) {
				lg = "zh";
			}
			
			// hebrew
			if (lg.equals("iw")) {
				lg = "he";
			}
			
			return lg;
		}
		
		public void swithReading() {
			if (this.retrievePageTask == null) {
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
			if (this.imageRepository != null && this.imageRepository.images != null && this.imageRepository.images.size() > 0) {
				this.mImgPrev.setVisibility(View.VISIBLE);
				this.mImgNext.setVisibility(View.VISIBLE);
				
				this.nextImage();
			}
		}
		
		@Override
		protected void onPause() {
			this.stopRequestLocation();
			this.pauseRead();
			super.onPause();
		}

		@Override
		protected void onResume() {
			this.startRequestLocation();
			super.onResume();
		}

		public View makeView() {
            ImageView iView = new ImageView(this);
            iView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            iView.setLayoutParams(new
                        ImageSwitcher.LayoutParams(
                                    LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT));
            return iView;
      }

		public void setCurrentTitle(String title) {
			if (title != null && this.mTitle != null && title.length() > 0) {
				this.mTitle.setText(title);
			}			
		}
		
		public Page getPage() {
			return this.page;
		}
		
		public void setHasResult(boolean hasResult) {
			if (hasResult) {
				this.noResult.setVisibility(View.GONE);
			} else {
				this.noResult.setVisibility(View.VISIBLE);
				this.mTitle.setText(getString(R.string.noresult));
			}
		}

		public void step() {
			if (this.reading) {
				this.readAtPosition();				
			}
			
			this.tryStopLocation();
			this.tryStartLocation();			
		}
		
		private void stepNext(int stepTime) {
			StepAsync step = new StepAsync(this);
			step.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, stepTime);
		}
		
		public Locale getLanguage() {
			if (currentLang == null) {
				this.setCurrentLang();
			}
			
			return currentLang;
		}
		
		private static final int TWO_MINUTES = 1000 * 60 * 2;

		/** Determines whether one Location reading is better than the current Location fix
		  * @param location  The new Location that you want to evaluate
		  * @param currentBestLocation  The current Location fix, to which you want to compare the new one
		  */
		protected boolean isBetterLocation(Location location, Location currentBestLocation) {
		    if (currentBestLocation == null) {
		        // A new location is always better than no location
		        return true;
		    }
		    
		    if (location == null) {
		    	return false;
		    }

		    // Check whether the new location fix is newer or older
		    long timeDelta = location.getTime() - currentBestLocation.getTime();
		    boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
		    boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
		    boolean isNewer = timeDelta > 0;

		    // If it's been more than two minutes since the current location, use the new location
		    // because the user has likely moved
		    if (isSignificantlyNewer) {
		        return true;
		    // If the new location is more than two minutes older, it must be worse
		    } else if (isSignificantlyOlder) {
		        return false;
		    }

		    // Check whether the new location fix is more or less accurate
		    int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
		    boolean isLessAccurate = accuracyDelta > 0;
		    boolean isMoreAccurate = accuracyDelta < 0;
		    boolean isSignificantlyLessAccurate = accuracyDelta > 200;

		    // Check if the old and new location are from the same provider
		    boolean isFromSameProvider = isSameProvider(location.getProvider(),
		            currentBestLocation.getProvider());

		    // Determine location quality using a combination of timeliness and accuracy
		    if (isMoreAccurate) {
		        return true;
		    } else if (isNewer && !isLessAccurate) {
		        return true;
		    } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
		        return true;
		    }
		    return false;
		}

		/** Checks whether two providers are the same */
		private boolean isSameProvider(String provider1, String provider2) {
		    if (provider1 == null) {
		      return provider2 == null;
		    }
		    return provider1.equals(provider2);
		}
		
		private void newLocation(Location newLocation) {
			if (isBetterLocation(newLocation, userLocation)) {
          	  userLocation = newLocation;
            }
		}
		
		private int tryBestLocation;
		
		private boolean requestLocationGpsStarted;
		private boolean requestLocationNetwordStarted;
		private long requestLocationTime;
		private void startRequestLocation()	 {			
			if (!this.requestLocationGpsStarted) {
				if(this.locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER )) {
		    		this.locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this.locationListener);
		    		this.requestLocationGpsStarted = true;		    		
		    	}				
			}			
			
	    	if (!this.requestLocationNetwordStarted) {
	    		if(this.locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
		    		// Register the listener with the Location Manager to receive location updates
		    		this.locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this.locationListener);
		    		this.requestLocationNetwordStarted = true;
		    	}
	    	}	    	
	    	
	    	if (this.isRequestLocationStarted()) {
	    		this.requestLocationTime = System.currentTimeMillis();
	    	}
		}
		
		private void stopRequestLocation() {
			if (this.isRequestLocationStarted()) {
				this.locationManager.removeUpdates(this.locationListener);
				this.requestLocationTime = System.currentTimeMillis();
				this.requestLocationGpsStarted = false;
				this.requestLocationNetwordStarted = false;
			}
		}
		
		private boolean isRequestLocationStarted() {
			return this.requestLocationGpsStarted || this.requestLocationNetwordStarted;
		}
		
		private static long requestTimerOn = 60000; // Request location on for 1 minute
		private static long requestTimerOff = 5 * 60000; // Request location off for 5 minutes
		
		private void tryStartLocation() {
			if (!this.isRequestLocationStarted()) {
				if (System.currentTimeMillis() - this.requestLocationTime > requestTimerOff) {
					this.startRequestLocation();
				}
			}			
		}
		
		private void tryStopLocation() {
			if (this.isRequestLocationStarted()) {
				if (System.currentTimeMillis() - this.requestLocationTime > requestTimerOn) {
					this.stopRequestLocation();
				}
			}
		}

		public void setPois(List<String> result) {			
			this.poiList = result;
			this.setInfoList(result);			
		}
		 
		public void setInfoList(List<String> result) {
			if (result != null) {
				for(String info : result) {
					if (!this.addInfoList(info)) {
						break;
					}
				}
				
				this.refreshInfoList();
			}						
		}
		
		public boolean addInfoList(String link) {
			if (link != null && link.length() > 0) {
				if (this.infoList.size() < maxLinksInfo) {
					this.infoList.add(link);					
					return true;
				}				
			}
			
			return false;
		}
		
		private void refreshInfoList() {
			this.infoListView.setAdapter(new ArrayAdapter<String>(this, R.layout.list_item, this.infoList));
			
			if (this.infoList != null && this.infoList.size() > 0) {							
				this.changeInfoView.setVisibility(View.VISIBLE);
			}
		}

		public void changeInfo() {
			this.showInfoList = !this.showInfoList;
			this.handleInfoListState();
		}	
		
		private void handleInfoListState() {
			if (this.showInfoList) {
				this.layoutInfoList.setVisibility(View.VISIBLE);
				this.main_noInfo.setVisibility(View.GONE);
				this.main_info.setVisibility(View.GONE);
			} else {
				this.layoutInfoList.setVisibility(View.GONE);			
				if (this.mainInfoVisible) {
					this.main_info.setVisibility(View.VISIBLE);
					this.main_noInfo.setVisibility(View.GONE);
				} else {
					this.main_info.setVisibility(View.GONE);
					this.main_noInfo.setVisibility(View.VISIBLE);
				}
			}
		}
		
		public void showInfo() {
			if (this.infoList != null && this.infoList.size() > 0) {
				this.showInfoList = true;
				this.handleInfoListState();
			}
		}

		public void setArticleLinks(List<LinkInfo> sortedLinks) {			
			if (sortedLinks != null) {
				int cpt = 0;
				for(LinkInfo info : sortedLinks) {
					if (!this.addInfoList(info.link.link) || cpt > maxLinksInfoGeo) {
						break;
					}
					
					cpt++;
				}
				
				this.refreshInfoList();
			}			
		}
}
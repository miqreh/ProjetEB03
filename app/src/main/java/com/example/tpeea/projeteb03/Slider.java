package com.example.tpeea.projeteb03;

/**
 * Created by Steph on 20/05/2018.
 */

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;




/**
 * Created by colas on 02/03/2018.
 */

public class Slider extends View {

    // dimensions minimales du widget (en dp)
    final static float DEFAULT_BAR_WIDTH = 10;
    final static float DEFAULT_BAR_LENGTH = 100;
    final static float DEFAULT_CURSOR_DIAMETER = 20;

    // définition des pinceaux
    private Paint mCursorPaint = null;
    private Paint mValueBarPaint = null;
    private Paint mBarPaint = null;

    // coloris du Slider
    private int mDisabledColor;
    private int mCursorColor;
    private int mBarColor;
    private int mValueBarColor;


    // direction par defaut du Slider
    private final static int TO_LEFT = 0;
    private final static int TO_RIGHT = 1;
    private final static int TO_TOP = 2;
    private final static int TO_BOTTOM = 3;

    private int mDirection = TO_TOP;


    // définition des dimensions (en pixel)
    private float mBarLength;
    private float mBarWidth;
    private float mCursorDiameter;

    private boolean mEnabled = true;
    private boolean mHorizontal;
    private boolean mReversed;


    // Valeur du Slider
    private float mValue = 0;
    // Borne min
    private float mMin = 0;
    // Borne max
    private float mMax = 100;


    // handler pour la gestion double clic
    private Handler mHandler = null;
    private boolean isDoubleClick = false;


    // Listener d'évènement de modification du Slider
    private SliderListener mSliderListener;


    public interface SliderListener {
        void onValueChanged(View view, float value);

        void onDoubleClick(View view, float value);
    }


    /*************************************************************************************/
    /*                        CONSTRUCTEURS et INITIALISATION                            */
    /*************************************************************************************/


    /**
     * Constructeur pour construction dynamique
     *
     * @param context
     */
    public Slider(Context context) {
        super(context);
        init(context,null);
    }

    /**
     * Constructeur pour construction statique
     *
     * @param context
     * @param attributeSet
     */
    public Slider(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        init(context, attributeSet);
    }


    /**
     * Initialisation des éléments du Slider afin d'éviter une surcharge de calcul lors d'une mise à jour graphique
     *
     * @param context      : le contexte dans lequel le slider est utilisé
     * @param attributeSet : les attributs définis dans le fichier XML
     */
    private void init(Context context, @Nullable AttributeSet attributeSet) {


        mHandler = new Handler();

        // Initialisation des dimensions en pixel
        mBarLength = dpToPixel(DEFAULT_BAR_LENGTH);
        mCursorDiameter = dpToPixel(DEFAULT_CURSOR_DIAMETER);
        mBarWidth = dpToPixel(DEFAULT_BAR_WIDTH);

        // intanciation des paints (par défaut)
        mCursorPaint = new Paint();
        mBarPaint = new Paint();
        mValueBarPaint = new Paint();

        // suppression du repliement
        mCursorPaint.setAntiAlias(true);
        mBarPaint.setAntiAlias(true);
        mValueBarPaint.setAntiAlias(true);

        // Application du style (plein)
        mValueBarPaint.setStyle(Paint.Style.STROKE);
        mBarPaint.setStyle(Paint.Style.STROKE);
        mCursorPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        // Spécification des terminaisons
        mBarPaint.setStrokeCap(Paint.Cap.ROUND);


        // default colors
        mDisabledColor = ContextCompat.getColor(context, R.color.colorDisabled);
        mCursorColor = ContextCompat.getColor(context, R.color.colorAccent);
        mBarColor = ContextCompat.getColor(context, R.color.colorPrimary);
        mValueBarColor = ContextCompat.getColor(context, R.color.colorSecondary);

        // récupération des personnalisations XML
        if (attributeSet != null) {
            TypedArray attr = context.obtainStyledAttributes(attributeSet, R.styleable.Slider, 0, 0);
            mBarLength = attr.getDimension(R.styleable.Slider_barLength, mBarLength);
            mBarWidth = attr.getDimension(R.styleable.Slider_barWidth, mBarWidth);
            mCursorDiameter = attr.getDimension(R.styleable.Slider_cursorDiameter, mCursorDiameter);

            mDirection = attr.getInt(R.styleable.Slider_direction, mDirection);
            mEnabled = !attr.getBoolean(R.styleable.Slider_disabled, !mEnabled);

            mDisabledColor = attr.getColor(R.styleable.Slider_barColor, mDisabledColor);
            mBarColor = attr.getColor(R.styleable.Slider_barColor, mBarColor);
            mValueBarColor = attr.getColor(R.styleable.Slider_valueColor, mValueBarColor);
            mCursorColor = attr.getColor(R.styleable.Slider_cursorColor, mCursorColor);

            mMin = attr.getFloat(R.styleable.Slider_min, mMin);
            mMax = attr.getFloat(R.styleable.Slider_max, mMax);
            mValue = attr.getFloat(R.styleable.Slider_value, mValue);

            attr.recycle();
        }



        if(mMin == 0){
            mValueBarPaint.setStrokeCap(Paint.Cap.ROUND);
        }



        // et finalement des couleurs (utilisation des couleurs du thème par défaut)
        if (mEnabled) {
            mCursorPaint.setColor(mCursorColor);
            mBarPaint.setColor(mBarColor);
            mValueBarPaint.setColor(mValueBarColor);
        } else {
            mCursorPaint.setColor(mDisabledColor);
            mBarPaint.setColor(mDisabledColor);
            mValueBarPaint.setColor(mDisabledColor);
        }


        // fixe les  largeurs
        mBarPaint.setStrokeWidth(mBarWidth);
        mValueBarPaint.setStrokeWidth(mBarWidth);


        // fixe la direction du Slider par défaut
        setDirection(mDirection);


    }


    /*************************************************************************************/
    /*                                       TRACE                                       */

    /*************************************************************************************/

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

//        Log.i("DRAW", "Width" + String.valueOf(getWidth()));
//        Log.i("DRAW", "Height" + String.valueOf(getHeight()));
//        Log.i("DRAW", "Padding left" + String.valueOf(reconciliateWidth()));
//        Log.i("DRAW", "Padding Top" + String.valueOf(reconciliateHeight()));
        // récupération de la place disponible
        setPadding(reconciliateWidth(), reconciliateHeight(), 0, 0);


        Point p1, p2;
        p1 = toPosition(mMin);
        p2 = toPosition(mMax);

        canvas.drawLine(p1.x, p1.y, p2.x, p2.y, mBarPaint);

        // positionnement du curseur et de la barre d'amplitude
        Point cursorPosition = toPosition(mValue);
        Point originPosition = toPosition(Math.max(0, mMin));

        if(!originPosition.equals(cursorPosition)) {
            canvas.drawLine(originPosition.x, originPosition.y, cursorPosition.x, cursorPosition.y, mValueBarPaint);
        }
        canvas.drawCircle(cursorPosition.x, cursorPosition.y, mCursorDiameter / 2, mCursorPaint);

    }


    /*************************************************************************************/
    /*                                       MESURE                                      */

    /*************************************************************************************/


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        Log.i("MEASURE", MeasureSpec.toString(widthMeasureSpec));
        Log.i("MEASURE", MeasureSpec.toString(heightMeasureSpec));


        int suggestedWidth, suggestedHeight, width, height;

        // Tente d'imposer une dimension minimale
        suggestedWidth = Math.max(getSuggestedMinimumWidth(), (int) (isHorizontal() ? mBarLength + mCursorDiameter : Math.max(mCursorDiameter, mBarWidth)) + getPaddingLeft() + getPaddingRight());
        suggestedHeight = Math.max(getSuggestedMinimumHeight(), (int) (isHorizontal() ? Math.max(mCursorDiameter, mBarWidth) : mBarLength + mCursorDiameter) + getPaddingTop() + getPaddingBottom());

        // Adapte la longueur et la largeur au minimum (en accroissant éventuellement les dimensions)
        if (isHorizontal()) {
            mCursorDiameter = suggestedHeight - getPaddingTop() - getPaddingBottom();
            mBarLength = suggestedWidth - getPaddingRight() - getPaddingLeft() - mCursorDiameter;
        } else {
            mCursorDiameter = suggestedWidth - getPaddingLeft() - getPaddingRight();
            mBarLength = suggestedHeight - getPaddingBottom() - getPaddingTop() - mCursorDiameter;
        }


        // cette méthode adapte la dimension demandée aux spécifications
        // Si la place est disponible, la suggestion est conservée sinon la contrainte est conservée
        width = resolveSize(suggestedWidth, widthMeasureSpec);
        height = resolveSize(suggestedHeight, heightMeasureSpec);
        setMeasuredDimension(width, height);
    }


    /**
     * Adapte la largeur du Widget et détermine le padding horizontal si la place est insuffisante
     */
    private int reconciliateWidth() {

        int leftPadding;

        if (isHorizontal()) {
            // pas assez de place
            if (getWidth() < mBarLength + mCursorDiameter) {
                mBarLength = getWidth() - mCursorDiameter;
                return 0;
            }
            if (getWidth() < mBarLength + mCursorDiameter + getPaddingLeft() + getPaddingRight()) {
                leftPadding = (int) ((getWidth() - mBarLength + mCursorDiameter) * getPaddingLeft() / (getPaddingRight() + getPaddingLeft()));
                return leftPadding;
            }
        } else {
            if (getWidth() < mCursorDiameter) {
                mCursorDiameter = getWidth();
                return 0;
            }

            if (getWidth() < mBarWidth) {
                mBarWidth = getWidth();
                return 0;
            }

            if (getWidth() < Math.max(mCursorDiameter, mBarWidth) + getPaddingLeft() + getPaddingRight()) {
                leftPadding = (int) ((getWidth() - Math.max(mCursorDiameter, mBarWidth)) * getPaddingLeft() / (getPaddingRight() + getPaddingLeft()));
                return leftPadding;
            }
        }
        return getPaddingLeft();

    }


    /**
     * Adapte la hauteur du Widget et détermine le padding horizontal si la place est insuffisante
     */
    private int reconciliateHeight() {

        int topPadding;

        if (!isHorizontal()) {
            // pas assez de place
            if (getHeight() < mBarLength + mCursorDiameter) {
                mBarLength = getHeight() - mCursorDiameter;
                return 0;
            }
            if (getHeight() < mBarLength + mCursorDiameter + getPaddingTop() + getPaddingBottom()) {
                topPadding = (int) ((getHeight() - mBarLength + mCursorDiameter) * getPaddingTop() / (getPaddingBottom() + getPaddingLeft()));
                return topPadding;
            }
        } else {
            if (getHeight() < mCursorDiameter) {
                mCursorDiameter = getHeight();
                return 0;
            }

            if (getHeight() < mBarWidth) {
                mBarWidth = getWidth();
                return 0;
            }

            if (getHeight() < Math.max(mCursorDiameter, mBarWidth) + getPaddingTop() + getPaddingBottom()) {
                topPadding = (int) ((getHeight() - Math.max(mCursorDiameter, mBarWidth)) * getPaddingBottom() / (getPaddingTop() + getPaddingBottom()));
                return topPadding;
            }
        }
        return getPaddingTop();

    }


    /**
     * Conversion d'un position écran en valeur Slider
     *
     * @param position : position à l'écran
     * @return : valeur Slider
     */
    private float toValue(Point position) {
        float ratio;
        if (isHorizontal())
            ratio = (position.x - getPaddingLeft() - mCursorDiameter / 2) / mBarLength;
        else
            ratio = (position.y - getPaddingTop() - mCursorDiameter / 2) / mBarLength;
        if (isReversed() ^ (!isHorizontal())) ratio = 1 - ratio;
        if (ratio < 0) ratio = 0;
        if (ratio > 1) ratio = 1;
        return ratioToValue(ratio);
    }


    private Point toPosition(float value) {
        int x, y, z;
        // à présent x1 et y1 représentent le centre du curseur
        y = (int) ((isReversed() ^ (!isHorizontal()) ? 1 - valueToRatio(value) : valueToRatio(value)) * mBarLength + mCursorDiameter / 2);
        x = (int) (Math.max(mCursorDiameter, mBarWidth) / 2);
        if (isHorizontal()) {
            z = x;
            x = y + getPaddingTop();
            y = z + getPaddingLeft();
        } else {
            x = x + getPaddingLeft();
            y = y + getPaddingTop();
        }

        return new Point(x, y);
    }

    /**
     * Convertit une valeur en dp en pixels
     *
     * @param valueInDp : valeur à convertir
     * @return : nombre de pixels correspondant
     */
    private float dpToPixel(float valueInDp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, valueInDp, getResources().getDisplayMetrics());
    }


    /**
     * Transforme la valeur du slider en une proportion (utile pour positionner le curseur)
     *
     * @return le ratio du slider
     */
    private float valueToRatio(float value) {
        return (value - mMin) / (mMax - mMin);
    }


    /**
     * transforme une proportion en valeur.
     *
     * @param ratio : proportion
     * @return
     */
    private float ratioToValue(float ratio) {
        return ratio * (mMax - mMin) + mMin;
    }


    private boolean isHorizontal() {
        return mHorizontal;
    }

    private boolean isReversed() {
        return mReversed;
    }


    /*************************************************************************************/
    /*                                       ACTION                                      */
    /*************************************************************************************/

    /**
     * Mémorisation de la référence sur le listener
     *
     * @param sliderListener : référence sur une instance de classe implémentant SliderListener
     */
    public void setSliderListener(SliderListener sliderListener) {
        mSliderListener = sliderListener;
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {


            case MotionEvent.ACTION_DOWN:
                if (isEnabled()) {
                    // gestion du double click
                    if (isDoubleClick) {
                        mValue = Math.max(0, mMin);
                        mSliderListener.onDoubleClick(this, mValue);
                        invalidate();

                    }
                    isDoubleClick = true;
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            isDoubleClick = false;
                        }
                    }, 300);
                    break;
                }
            case MotionEvent.ACTION_MOVE:
                if (isEnabled()) {
                    mValue = toValue(new Point((int) event.getX(), (int) event.getY()));
                    mSliderListener.onValueChanged(this, mValue);
                    invalidate();
                    break;
                }

                break;
            case MotionEvent.ACTION_UP:
                break;


        }


        return true;
    }


    /*************************************************************************************/
    /*                                       SETTERS et GETTERS                          */

    /*************************************************************************************/


    public float getValue() {
        return mValue;
    }

    /**
     * Détermine la direction du Slider
     *
     * @param direction : enumération désignant la direction
     */
    public void setDirection(int direction) {
        mDirection = direction;
        mHorizontal = (mDirection == TO_LEFT || mDirection == TO_RIGHT);
        mReversed = (mDirection == TO_LEFT || mDirection == TO_BOTTOM);

        // initialisation des dimensions minimales (incluent)
        float minWidth = Math.max(dpToPixel(DEFAULT_BAR_WIDTH), dpToPixel(DEFAULT_CURSOR_DIAMETER));
        float minHeight = dpToPixel(DEFAULT_BAR_LENGTH + DEFAULT_CURSOR_DIAMETER);
        float swap;


        if (isHorizontal()) {
            swap = minWidth;
            minWidth = minHeight;
            minHeight = swap;
        }
        setMinimumHeight((int) minHeight + getPaddingTop() + getPaddingBottom());
        setMinimumWidth((int) minWidth + getPaddingLeft() + getPaddingRight());


    }

    /**
     * Fixe la valeur du Slider
     *
     * @param value : valeur entre min et max
     */
    public void setValue(float value) {
        mValue = value;
    }


    /**
     * Fixe la borne min du Slider
     *
     * @param min
     */
    public void setMin(float min) {
        mMin = min;
    }

    /**
     * Fixe la borne max du Slider
     *
     * @param max
     */
    public void setMax(float max) {
        mMax = max;
    }


    @Override
    public boolean isEnabled() {
        return mEnabled;

    }

    /**
     * Active le Slider
     */
    public void disable() {
        mEnabled = false;
        mCursorPaint.setColor(mDisabledColor);
        mBarPaint.setColor(mDisabledColor);
        mValueBarPaint.setColor(mDisabledColor);
        invalidate();
    }


    /**
     * Desactive le Slider, il n'es plus modifiable par l'utilisateur
     */
    public void enable() {
        mEnabled = true;
        mCursorPaint.setColor(mCursorColor);
        mBarPaint.setColor(mBarColor);
        mValueBarPaint.setColor(mValueBarColor);
        invalidate();
    }


    /*************************************************************************************/
    /*                               PERSISTANCE D'ETAT                                  */

    /*************************************************************************************/
    @Nullable
    @Override
    protected Parcelable onSaveInstanceState() {
        // récupération des données déja agglomérées
        return new SavedState(super.onSaveInstanceState(), mValue);
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (!(state instanceof SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }
        mValue = ((SavedState) state).sliderValue;
        super.onRestoreInstanceState(((SavedState) state).getSuperState());
        Log.i("RESTORE", "state restored with " + mValue);
    }


    static class SavedState extends BaseSavedState {

        private float sliderValue;

        // Factory permettant la reconstruction du parcelable SavedState
        public static final Parcelable.Creator<SavedState> CREATOR =
                new Parcelable.Creator<SavedState>() {
                    public SavedState createFromParcel(Parcel in) {
                        return new SavedState(in);
                    }

                    public SavedState[] newArray(int size) {
                        return new SavedState[size];
                    }
                };

        // constructeur privé utilisé par la factory pour restaurer l'objet
        // les données doivent être lues dans le parcel dans le même ordre qu'elles sont écrites
        private SavedState(Parcel source) {
            super(source);
            sliderValue = source.readFloat();
        }

        public SavedState(Parcelable superState, float value) {
            super(superState);
            sliderValue = value;
        }

        // fournit la démarche de création du parcel
        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeFloat(sliderValue);
        }


    }


}

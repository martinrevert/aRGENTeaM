/*
 * aRGENTeaM for Android.
 * Copyright (C) 2013 Martin Revert
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package ar.com.martinrevert.argenteam;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import java.util.TreeMap;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.fedorvlasov.lazylist.ImageLoader;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;

import android.util.Log;

import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.ScrollView;
import android.widget.TextView;

public class Peli extends CustomMenu implements OnClickListener {

    private ImageLoader imageLoader;
    private String message;
    private ImageView image;
    private ImageButton youtu;


    public String detalle;
    public String titul;
    public String post;
    public String yout;
    public String subtitulo;
    public String release;
    public String key;
    public String subtitle;
    public String sub;



    Map<String, String> movie = new HashMap<String, String>();
    TreeMap<String, String> elinks = new TreeMap<String, String>();
    Map<String, String> torrents = new HashMap<String, String>();

    public String mula;
    public String btntxt;
    public String rating;
    private boolean p2p;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences preferencias = PreferenceManager
                .getDefaultSharedPreferences(getBaseContext());
        p2p = preferencias.getBoolean("p2p", false);
        image = new ImageView(Peli.this);
        image.setId(99995);
        imageLoader = new ImageLoader(getBaseContext(), "movie");

        message = getIntent().getStringExtra("passed");

        if (isOnline()) {
            new GetPage().execute(message);
        } else {
            vibrateToast(R.string.sininternet);
            finish();

        }
    }

    private class GetPage extends AsyncTask<String, Void, Integer> {

        ProgressDialog dialog = new ProgressDialog(Peli.this);
        private String pegaitems;
        private Button btnelink;
        private String terminacion;


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog.setMessage("Cargando...");
            dialog.show();
        }

        @Override
        protected Integer doInBackground(String... query) {

            Document doc = null;
            int numtries = 3;
            while (true) {

                try {
                    doc = Jsoup.connect(query[0]).timeout(60000)
                            .cookie("tca", "Y").get();
                    break;
                } catch (Exception e) {

                    e.printStackTrace();
                    if (--numtries == 0)
                        try {
                            throw e;
                        } catch (Exception e1) {
                            e1.printStackTrace();
                            return 0;
                        }
                }
            }
            if (doc.select("div.details").first().text() != null) {

                String deta = doc.select("div.details").first().text();
                detalle = Jsoup.parse(deta).text();
                Log.v("DETAILS", detalle);
            }

        //    Element poster = doc.select("div.pmovie > img").first();
        //    post = poster.attr("src");
        //    Log.v("POSTER", post);

            if (doc.select("div.pmovie > img").first() == null) {
                post = "http://www.argenteam.net/images/header-background.gif";
            } else {
                Element poster = doc.select("div.pmovie > img").first();
                post = poster.attr("src");
            }
            Log.v("POSTER", post);

            Element titulo = doc.select("div.pmovie > h1").first();
            titul = titulo.text();
            Log.v("TITULO", titul);

            //ToDo IMDB???
         /*   Elements im = doc.select("div.section scene-info");
            String imdb = im.text();
            Log.v("IMDB", imdb);*/

            if (doc.select("div.score").first() != null) {
                Element puntaje = doc.select("div.score").first();
                rating = puntaje.text();
                rating = rating.substring(0, 1) + "." + rating.substring(1, rating.length());
            } else {
                rating = "Sin puntaje";
            }
            Log.v("RATING", rating);

            if (doc.select(
                    "div.pmovie > div.media > div.trailer > div.media-content > object > param")
                    .first() == null) {
                if (doc.select(
                        "div.pmovie > div.media > div.trailer-single > div.media-content > object > param")
                        .first() == null) {
                    yout = "bla";
                } else {
                    Element youtube = doc
                            .select("div.pmovie > div.media > div.trailer-single > div.media-content > object > param")
                            .first();
                    yout = youtube.attr("value");
                }

            } else {
                Element youtube = doc
                        .select("div.pmovie > div.media > div.trailer > div.media-content > object > param")
                        .first();
                yout = youtube.attr("value");

            }

            Log.v("YOUTUBE", yout);

            Iterator<Element> items = doc.select("div.pmovie > div.item")
                    .iterator();

            StringBuilder sb = new StringBuilder();

            while (items.hasNext()) {
                Element version = items.next();
                String vers = version.text();
                sb.append(vers);
                sb.append("\n");

            }
            pegaitems = sb.toString();

            Iterator<Element> releases = doc.select(
                    "div.pmovie > div.releases > div.release").iterator();
            Iterator<Element> subt = doc.select("a[href^=/subtitles]")
                    .iterator();


            while (subt.hasNext()) {

                Element subti = subt.next();
                subtitulo = subti.attr("href");
                Log.v("SUBTITULO", subtitulo);

                if (subtitulo.endsWith("CC")) {
                    Log.v("CC", "NO");

                }else{

                    Element rel = releases.next();
                    release = rel.text();
                    if (release.contains("Streaming")){
                        Log.v("RELEASES", "PUM");
                        rel = releases.next();
                        release = rel.text();
                    }
                    Log.v("RELEASES", release);
                    movie.put(release, subtitulo);
                }


            }

            Elements ed2k = doc.select("a[href^=ed2k]");

            for (Element emule : ed2k) {

                mula = emule.attr("href");
                // palito = mula.indexOf("|");
                String previo = mula.substring(13);
                int palito = 0;
                String theend = previo.substring(palito, previo.indexOf("|"));
                Log.v("LINKS", mula);
                Log.v("LINKS", theend);
                elinks.put(mula, theend);

            }

			/*
             * if (Build.VERSION.SDK_INT > Build.VERSION_CODES.FROYO) {
			 * elinks.descendingMap(); }
			 */
            Elements bittorents = doc.select("a[href^=magnet]");

            for (Element torrent : bittorents) {

                String torro = torrent.attr("href");

                try {
                    String previo = torro.substring(64);
                    terminacion = previo.substring(0, previo.indexOf("&"));
                    if (terminacion.equalsIgnoreCase("acker.publichd.eu/announce")) {
                        terminacion = "Magnet Link sin nombre";
                    }
                } catch (Exception e) {
                    terminacion = "Magnet link";
                } finally {
                    Log.v("LINKS TORRENT", torro);
                    Log.v("LINKS TORRENT", terminacion);
                    torrents.put(torro, terminacion);
                }
            }

            return 1;
        }// Fin doinbackground

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            dialog.dismiss();

            if (result == 0) {
                vibrateToast(R.string.sinportal);
                finish();
            }

            TextView titulo = new TextView(Peli.this);
            titulo.setText(titul);
            titulo.setId(99999);
            titulo.setTextColor(0xffFF992B);
            titulo.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);

            RatingBar rate = new RatingBar(Peli.this, null, android.R.attr.ratingBarStyleSmall);
            if (rating.equals("Sin puntaje"))
            {rate.setRating(0.0f);}
            else{
            rate.setRating (Float.parseFloat(rating)/2);}
            rate.setNumStars(10);
            rate.setMax(10);
            rate.setId(99987);
            rate.setStepSize((float)0.01);
            rate.setIsIndicator(true);


            TextView detall = new TextView(Peli.this);
            detall.setText(detalle);
            detall.setId(99998);
            detall.setTextColor(0xffFFFFFF);

            TextView datos = new TextView(Peli.this);
            datos.setText(pegaitems);
            datos.setId(99997);

            youtu = new ImageButton(Peli.this);
            youtu.setImageResource(R.drawable.ic_youtube);
            youtu.setId(99975);

            TextView sinopsis = new TextView(Peli.this);
            sinopsis.setText(R.string.plot);
            sinopsis.setId(99990);
            sinopsis.setTextColor(0xffFF992B);
            sinopsis.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);

            TextView downlsubs = new TextView(Peli.this);
            downlsubs.setText(R.string.downloadsubs);
            downlsubs.setId(99989);
            downlsubs.setTextColor(0xffFF992B);
            downlsubs.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);

            TextView downltorrents = new TextView(Peli.this);
            downltorrents.setText(R.string.downloadtorrents);
            downltorrents.setId(99986);
            downltorrents.setTextColor(0xffFF992B);
            downltorrents.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);

            TextView downlelinks = new TextView(Peli.this);
            downlelinks.setText(R.string.downloademule);
            downlelinks.setId(99988);
            downlelinks.setTextColor(0xffFF992B);
            downlelinks.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);

            imageLoader.DisplayImage(post, image);

            ScrollView scrollview = new ScrollView(Peli.this);
            RelativeLayout relativelayout = new RelativeLayout(Peli.this);
            relativelayout.setId(100000);

            RelativeLayout.LayoutParams paramsimage = new RelativeLayout.LayoutParams(
                    LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            paramsimage.addRule(RelativeLayout.BELOW, rate.getId());
            paramsimage.addRule(RelativeLayout.ALIGN_LEFT);

            paramsimage.width = 320;
            paramsimage.height = 472;

            image.setLayoutParams(paramsimage);

            RelativeLayout layoutsubs = new RelativeLayout(Peli.this);
            layoutsubs.setId(100001);
            RelativeLayout.LayoutParams paramssubs = new RelativeLayout.LayoutParams(
                    LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            paramssubs.addRule(RelativeLayout.BELOW, downlsubs.getId());
            layoutsubs.setLayoutParams(paramssubs);

            RelativeLayout layoutelinks = new RelativeLayout(Peli.this);
            layoutelinks.setId(100002);
            RelativeLayout.LayoutParams paramselinks = new RelativeLayout.LayoutParams(
                    LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            paramselinks.addRule(RelativeLayout.BELOW, downlelinks.getId());
            layoutelinks.setLayoutParams(paramselinks);

            RelativeLayout layoutetorrents = new RelativeLayout(Peli.this);
            layoutetorrents.setId(50000);
            RelativeLayout.LayoutParams paramstorrents = new RelativeLayout.LayoutParams(
                    LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            paramstorrents.addRule(RelativeLayout.BELOW, downltorrents.getId());
            layoutetorrents.setLayoutParams(paramstorrents);

            RelativeLayout.LayoutParams tituloparams = new RelativeLayout.LayoutParams(
                    LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            tituloparams.addRule(RelativeLayout.ALIGN_TOP);
            titulo.setLayoutParams(tituloparams);

            RelativeLayout.LayoutParams subtiparams = new RelativeLayout.LayoutParams(
                    LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            subtiparams.addRule(RelativeLayout.BELOW, detall.getId());
            downlsubs.setLayoutParams(subtiparams);

            RelativeLayout.LayoutParams elinkparams = new RelativeLayout.LayoutParams(
                    LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            elinkparams.addRule(RelativeLayout.BELOW, layoutsubs.getId());
            downlelinks.setLayoutParams(elinkparams);

            RelativeLayout.LayoutParams torrentparams = new RelativeLayout.LayoutParams(
                    LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            torrentparams.addRule(RelativeLayout.BELOW, layoutelinks.getId());
            downltorrents.setLayoutParams(torrentparams);

            RelativeLayout.LayoutParams sinopsisparams = new RelativeLayout.LayoutParams(
                    LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            sinopsisparams.addRule(RelativeLayout.BELOW, image.getId());
            sinopsis.setLayoutParams(sinopsisparams);

            RelativeLayout.LayoutParams ratingparams = new RelativeLayout.LayoutParams(
                    LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            ratingparams.addRule(RelativeLayout.BELOW, titulo.getId());
            ratingparams.addRule(RelativeLayout.ALIGN_LEFT);
            rate.setLayoutParams(ratingparams);

            RelativeLayout.LayoutParams paramsdetall = new RelativeLayout.LayoutParams(
                    LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            paramsdetall.addRule(RelativeLayout.BELOW, sinopsis.getId());
            detall.setLayoutParams(paramsdetall);

            RelativeLayout.LayoutParams paramsdatos = new RelativeLayout.LayoutParams(
                    LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            paramsdatos.addRule(RelativeLayout.RIGHT_OF, image.getId());
            paramsdatos.addRule(RelativeLayout.BELOW, rate.getId());
            datos.setLayoutParams(paramsdatos);

            RelativeLayout.LayoutParams paramsyoutu = new RelativeLayout.LayoutParams(
                    LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            paramsyoutu.addRule(RelativeLayout.RIGHT_OF, image.getId());
            paramsyoutu.addRule(RelativeLayout.BELOW, datos.getId());
            youtu.setLayoutParams(paramsyoutu);

            relativelayout.addView(titulo);
            relativelayout.addView(image);
            relativelayout.addView(rate);
            relativelayout.addView(detall);
            relativelayout.addView(datos);

            if (yout != "bla") {
                relativelayout.addView(youtu);
            }
            relativelayout.addView(sinopsis);
            relativelayout.addView(downlsubs);
            relativelayout.addView(downlelinks);
            relativelayout.addView(downltorrents);

            Button btnsub = null;
            Button btntorrent = null;

            int k = 1;
            int m = 1000;
            int t = 2000;
            for (Entry<String, String> entry : movie.entrySet()) {
                key = entry.getKey();
                sub = entry.getValue();
                btnsub = new Button(Peli.this);
                btnsub.setText(key);
                btnsub.setId(k);
                btnsub.setTag(sub);
                btnsub.setOnClickListener(Peli.this);

                if (k == 1) {
                    RelativeLayout.LayoutParams lay1 = new RelativeLayout.LayoutParams(
                            RelativeLayout.LayoutParams.MATCH_PARENT,
                            RelativeLayout.LayoutParams.WRAP_CONTENT);
                    lay1.addRule(RelativeLayout.ALIGN_PARENT_TOP,
                            titulo.getId());

                    layoutsubs.addView(btnsub, lay1);
                } else {
                    RelativeLayout.LayoutParams lay2 = new RelativeLayout.LayoutParams(
                            RelativeLayout.LayoutParams.MATCH_PARENT,
                            RelativeLayout.LayoutParams.WRAP_CONTENT);
                    lay2.addRule(RelativeLayout.BELOW, btnsub.getId() - 1);
                    layoutsubs.addView(btnsub, lay2);
                }
                k++;

            } // fin for movie

            for (Entry<String, String> ocurr : elinks.entrySet()) {
                String elinkbtn = ocurr.getKey();
                String txtbtn = ocurr.getValue();
                btnelink = new Button(Peli.this);
                btnelink.setId(m);
                btnelink.setTag(elinkbtn);
                btnelink.setText(txtbtn);
                btnelink.setOnClickListener(Peli.this);

                if (m == 1000) {
                    RelativeLayout.LayoutParams lay3 = new RelativeLayout.LayoutParams(
                            RelativeLayout.LayoutParams.MATCH_PARENT,
                            RelativeLayout.LayoutParams.WRAP_CONTENT);
                    lay3.addRule(RelativeLayout.ALIGN_PARENT_TOP,
                            RelativeLayout.TRUE);

                    layoutelinks.addView(btnelink, lay3);
                } else {
                    RelativeLayout.LayoutParams lay4 = new RelativeLayout.LayoutParams(
                            RelativeLayout.LayoutParams.MATCH_PARENT,
                            RelativeLayout.LayoutParams.WRAP_CONTENT);
                    lay4.addRule(RelativeLayout.BELOW, btnelink.getId() - 1);
                    layoutelinks.addView(btnelink, lay4);
                }
                m++;
            } // Fin for elinks

            for (Entry<String, String> ocurrencia : torrents.entrySet()) {
                String torrentbtn = ocurrencia.getKey();
                String txttorrbtn = ocurrencia.getValue();
                btntorrent = new Button(Peli.this);
                btntorrent.setId(t);
                btntorrent.setTag(torrentbtn);
                btntorrent.setText(txttorrbtn);
                btntorrent.setOnClickListener(Peli.this);

                if (t == 2000) {
                    RelativeLayout.LayoutParams lay5 = new RelativeLayout.LayoutParams(
                            RelativeLayout.LayoutParams.MATCH_PARENT,
                            RelativeLayout.LayoutParams.WRAP_CONTENT);
                    lay5.addRule(RelativeLayout.ALIGN_PARENT_TOP,
                            RelativeLayout.TRUE);

                    layoutetorrents.addView(btntorrent, lay5);
                } else {
                    RelativeLayout.LayoutParams lay6 = new RelativeLayout.LayoutParams(
                            RelativeLayout.LayoutParams.MATCH_PARENT,
                            RelativeLayout.LayoutParams.WRAP_CONTENT);
                    lay6.addRule(RelativeLayout.BELOW, btntorrent.getId() - 1);
                    layoutetorrents.addView(btntorrent, lay6);
                }
                t++;
            } // Fin for torrents

            relativelayout.addView(layoutsubs);
            relativelayout.addView(layoutelinks);
            relativelayout.addView(layoutetorrents);
            scrollview.addView(relativelayout);
            setContentView(scrollview);

            youtu.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View arg0) {

                    Log.v("YOUTUBE", "Click!");
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri
                            .parse(yout)));
                }
            });

        }// Fin onPostExecute
    }// Fin asyctask

    @Override
    public void onClick(View v) {

        if (v.getId() >= 1 && v.getId() < 1000) {

            // String indice = String.valueOf(v.getId());
            String tag = (String) v.getTag();
            String URIpost = "http://www.argenteam.net" + tag;
            Intent subintent = new Intent(Peli.this, DownloadFile.class);
            subintent.putExtra("passed", URIpost);
            Peli.this.startActivityForResult(subintent, 0);

        } else {
            if (v.getId() >= 2000 && v.getId() < 3000 && !p2p) {
                Log.v("TORRENT", "BOTON");
                String URIpost = (String) v.getTag();
                Intent subintent = new Intent(Peli.this,
                        BittorrentRequest.class);
                subintent.putExtra("passed", URIpost);
                Peli.this.startActivityForResult(subintent, 0);
            } else {
                if (v.getId() >= 1000 && v.getId() < 2000 && !p2p) {
                    Log.v("ELINK", "BOTON!");
                    String URIpost = (String) v.getTag();
                    Intent subintent = new Intent(Peli.this, EmuleRequest.class);
                    subintent.putExtra("passed", URIpost);
                    Peli.this.startActivityForResult(subintent, 0);

                } else {

                    String tag = (String) v.getTag();
                    if (isP2P(tag)) {
                        Intent sharingIntent = new Intent(
                                android.content.Intent.ACTION_VIEW);
                        sharingIntent
                                .addCategory(android.content.Intent.CATEGORY_BROWSABLE);
                        sharingIntent.setData(Uri.parse(tag));
                        startActivity(sharingIntent);
                    } else {

                        vibrateToast(R.string.sinp2papp);
                    }
                }
            }
        }

    }
}

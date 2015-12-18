package net.medialabs.functions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

public class GoogleParser extends XMLParser implements Parser {

    private int distance;

    public GoogleParser(String feedUrl) {
            super(feedUrl);
    }

    public Route parse() {
            final String result = convertStreamToString(this.getInputStream());
            final Route route = new Route();
            final Segment segment = new Segment();
            try {
                    final JSONObject json = new JSONObject(result);
                    final JSONObject jsonRoute = json.getJSONArray("routes").getJSONObject(0);
                    final JSONObject leg = jsonRoute.getJSONArray("legs").getJSONObject(0);
                    final JSONArray steps = leg.getJSONArray("steps");
                    final int numSteps = steps.length();
                    route.setName(leg.getString("start_address") + " to " + leg.getString("end_address"));
                    route.setCopyright(jsonRoute.getString("copyrights"));
                    route.setLength(leg.getJSONObject("distance").getInt("value"));
                    if (!jsonRoute.getJSONArray("warnings").isNull(0)) {
                            route.setWarning(jsonRoute.getJSONArray("warnings").getString(0));
                    }

                    for (int i = 0; i < numSteps; i++) {
                            final JSONObject step = steps.getJSONObject(i);
                            final JSONObject start = step.getJSONObject("start_location");
                            final LatLng position = new LatLng(start.getDouble("lat"),
                                    start.getDouble("lng"));
                            segment.setPoint(position);
                            final int length = step.getJSONObject("distance").getInt("value");
                            distance += length;
                            segment.setLength(length);
                            segment.setDistance(distance/1000);
                            segment.setInstruction(step.getString("html_instructions").replaceAll("<(.*?)*>", ""));
                            route.addPoints(decodePolyLine(step.getJSONObject("polyline").getString("points")));
                            route.addSegment(segment.copy());
                    }
            } catch (JSONException e) {
                    Log.e(e.getMessage(), "Google JSON Parser - " + feedUrl);
            }
            return route;
    }

    private static String convertStreamToString(final InputStream input) {
    final BufferedReader reader = new BufferedReader(new InputStreamReader(input));
    final StringBuilder sBuf = new StringBuilder();

    String line = null;
    try {
        while ((line = reader.readLine()) != null) {
            sBuf.append(line);
        }
    } catch (IOException e) {
            Log.e(e.getMessage(), "Google parser, stream2string");
    } finally {
        try {
            input.close();
        } catch (IOException e) {
            Log.e(e.getMessage(), "Google parser, stream2string");
        }
    }
    return sBuf.toString();
}

    private List<LatLng> decodePolyLine(final String poly) {
            int len = poly.length();
            int index = 0;
            List<LatLng> decoded = new ArrayList<LatLng>();
            int lat = 0;
            int lng = 0;

            while (index < len) {
            int b;
            int shift = 0;
            int result = 0;
            do {
                    b = poly.charAt(index++) - 63;
                    result |= (b & 0x1f) << shift;
                    shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                    b = poly.charAt(index++) - 63;
                    result |= (b & 0x1f) << shift;
                    shift += 5;
            } while (b >= 0x20);
                    int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
                    lng += dlng;

            decoded.add(new LatLng(
                    lat/100000d, lng/100000d
                    ));
            }

            return decoded;
            }


}

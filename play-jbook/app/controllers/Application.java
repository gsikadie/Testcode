package controllers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import com.fasterxml.jackson.databind.JsonNode;

import play.mvc.*;

import play.libs.F.Promise;
import play.libs.WS; // for web service calls

import play.mvc.Result; //contains the results for node.js server that need to be parsed.
import static play.libs.F.Function;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

public class Application extends Controller {
	public static Result index() {

		// return ok(views.html.index.render("ah ah ah code forever..."));
		Promise<WS.Response> homePage = WS.url("http://localhost:8000").get();
		// ret
		Map<String, String> itWorks = new HashMap<String, String>();
		itWorks.put("KO", "You are at root , try other requests");
		return ok(play.libs.Json.toJson(itWorks));
	}

	public static Promise<Result> users() {
		final Promise<Result> resultPromise = WS.url("http://localhost:8000/api/users").get()
				.map(new Function<WS.Response, Result>() {
					public Result apply(WS.Response response) {
						return ok(response.asJson());
					}
				});
		return resultPromise;
	}



	public static Result purchase(String username) {
		List<Map<String, Object>> itWorks = new ArrayList<Map<String, Object>>();
		try {
			HttpResponse<com.mashape.unirest.http.JsonNode> responseOne = Unirest
					.get("http://localhost:8000/api/users/" + username).asJson();
			if (responseOne.getBody().toString().equals("{}")) {
				return ok("User with username of " + username + " was not found");
			} else {
				// Fetch 5 recent purchases for user
				HttpResponse<com.mashape.unirest.http.JsonNode> responseTwo = Unirest
						.get("http://localhost:8000/api/purchases/by_user/" + username + "?limit=5").asJson();
				JSONArray fiveRecentPurchases = responseTwo.getBody().getObject().getJSONArray("purchases");

				// Here i split the 5 purchases and
				// compose my Json
				for (int i = 0; i < fiveRecentPurchases.length(); i++) {
					Map node = new HashMap();
					node.put("id", fiveRecentPurchases.getJSONObject(i).getInt("productId"));

					// Get informations on the
					// product
					HttpResponse<com.mashape.unirest.http.JsonNode> responseThree = Unirest
							.get("http://localhost:8000/api/products/"
									+ fiveRecentPurchases.getJSONObject(i).getInt("productId"))
							.asJson();
					JSONObject productInfo = responseThree.getBody().getObject().getJSONObject("product");
					System.out.println("TOOOOOOOOOOOOTO" + productInfo);
					node.put("face", productInfo.getString("face"));
					node.put("price", productInfo.getInt("price"));
					node.put("size", productInfo.getInt("size"));

					HttpResponse<com.mashape.unirest.http.JsonNode> responseFour = Unirest
							.get("http://localhost:8000/api/purchases/by_product/"
									+ fiveRecentPurchases.getJSONObject(i).getInt("productId"))
							.asJson();
					JSONArray recentPurchase = responseFour.getBody().getObject().getJSONArray("purchases");
					List recentBuyers = new ArrayList<>();
					for (int j = 0; j < recentPurchase.length(); j++) {
						recentBuyers.add(recentPurchase.getJSONObject(j).getString("username"));
					}
					node.put("recent", recentBuyers);
					// sort itwork in descending order on size field

					itWorks.add(node);
				}
				Collections.sort(itWorks, new Comparator<Map<String, Object>>() {
					@Override
					public int compare(final Map<String, Object> map1, final Map<String, Object> map2) {
						// Get fields from maps, compare
						return Integer.parseInt(map2.get("size").toString())
								- Integer.parseInt(map1.get("size").toString());
					}
				});
			}

		} catch (UnirestException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ok(play.libs.Json.toJson(itWorks));
	}

}

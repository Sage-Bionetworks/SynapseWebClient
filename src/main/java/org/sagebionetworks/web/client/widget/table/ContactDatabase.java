package org.sagebionetworks.web.client.widget.table;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gwt.user.client.Random;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.ListDataProvider;

/**
 * The data source for contact information used in the sample.
 */
public class ContactDatabase {

  /**
   * A contact category.
   */
  public static class Category {

    private final String displayName;

    private Category(String displayName) {
      this.displayName = displayName;
    }

    public String getDisplayName() {
      return displayName;
    }
  }

  private static final String[] DRUG1_NAMES = { "5155877", "digitoxigenin", "dosulepin", "alverine", "anisomycin", "astemizole", "atovaquone" };
  private static final String[] DRUG2_NAMES = { "Emetine", "GW 8510", "MG 132" };
  
  
  private static final String[] FEMALE_FIRST_NAMES = {
      "Mary", "Patricia", "Linda", "Barbara", "Elizabeth", "Jennifer", "Maria", "Susan",
      "Margaret", "Dorothy", "Lisa", "Nancy", "Karen", "Betty", "Helen", "Sandra", "Donna",
      "Carol", "Ruth", "Sharon", "Michelle", "Laura", "Sarah", "Kimberly", "Deborah", "Jessica",
      "Shirley", "Cynthia", "Angela", "Melissa", "Brenda", "Amy", "Anna", "Rebecca", "Virginia",
      "Kathleen", "Pamela", "Martha", "Debra", "Amanda", "Stephanie", "Carolyn", "Christine",
      "Marie", "Janet", "Catherine", "Frances", "Ann", "Joyce", "Diane", "Alice", "Julie",
      "Heather", "Teresa", "Doris", "Gloria", "Evelyn", "Jean", "Cheryl", "Mildred", "Katherine",
      "Joan", "Ashley", "Judith", "Rose", "Janice", "Kelly", "Nicole", "Judy", "Christina",
      "Kathy", "Theresa", "Beverly", "Denise", "Tammy", "Irene", "Jane", "Lori", "Rachel",
      "Marilyn", "Andrea", "Kathryn", "Louise", "Sara", "Anne", "Jacqueline", "Wanda", "Bonnie",
      "Julia", "Ruby", "Lois", "Tina", "Phyllis", "Norma", "Paula", "Diana", "Annie", "Lillian",
      "Emily", "Robin", "Peggy", "Crystal", "Gladys", "Rita", "Dawn", "Connie", "Florence",
      "Tracy", "Edna", "Tiffany", "Carmen", "Rosa", "Cindy", "Grace", "Wendy", "Victoria", "Edith",
      "Kim", "Sherry", "Sylvia", "Josephine", "Thelma", "Shannon", "Sheila", "Ethel", "Ellen",
      "Elaine", "Marjorie", "Carrie", "Charlotte", "Monica", "Esther", "Pauline", "Emma",
      "Juanita", "Anita", "Rhonda", "Hazel", "Amber", "Eva", "Debbie", "April", "Leslie", "Clara",
      "Lucille", "Jamie", "Joanne", "Eleanor", "Valerie", "Danielle", "Megan", "Alicia", "Suzanne",
      "Michele", "Gail", "Bertha", "Darlene", "Veronica", "Jill", "Erin", "Geraldine", "Lauren",
      "Cathy", "Joann", "Lorraine", "Lynn", "Sally", "Regina", "Erica", "Beatrice", "Dolores",
      "Bernice", "Audrey", "Yvonne", "Annette", "June", "Samantha", "Marion", "Dana", "Stacy",
      "Ana", "Renee", "Ida", "Vivian", "Roberta", "Holly", "Brittany", "Melanie", "Loretta",
      "Yolanda", "Jeanette", "Laurie", "Katie", "Kristen", "Vanessa", "Alma", "Sue", "Elsie",
      "Beth", "Jeanne"};
  private static final String[] MALE_FIRST_NAMES = {
      "James", "John", "Robert", "Michael", "William", "David", "Richard", "Charles", "Joseph",
      "Thomas", "Christopher", "Daniel", "Paul", "Mark", "Donald", "George", "Kenneth", "Steven",
      "Edward", "Brian", "Ronald", "Anthony", "Kevin", "Jason", "Matthew", "Gary", "Timothy",
      "Jose", "Larry", "Jeffrey", "Frank", "Scott", "Eric", "Stephen", "Andrew", "Raymond",
      "Gregory", "Joshua", "Jerry", "Dennis", "Walter", "Patrick", "Peter", "Harold", "Douglas",
      "Henry", "Carl", "Arthur", "Ryan", "Roger", "Joe", "Juan", "Jack", "Albert", "Jonathan",
      "Justin", "Terry", "Gerald", "Keith", "Samuel", "Willie", "Ralph", "Lawrence", "Nicholas",
      "Roy", "Benjamin", "Bruce", "Brandon", "Adam", "Harry", "Fred", "Wayne", "Billy", "Steve",
      "Louis", "Jeremy", "Aaron", "Randy", "Howard", "Eugene", "Carlos", "Russell", "Bobby",
      "Victor", "Martin", "Ernest", "Phillip", "Todd", "Jesse", "Craig", "Alan", "Shawn",
      "Clarence", "Sean", "Philip", "Chris", "Johnny", "Earl", "Jimmy", "Antonio", "Danny",
      "Bryan", "Tony", "Luis", "Mike", "Stanley", "Leonard", "Nathan", "Dale", "Manuel", "Rodney",
      "Curtis", "Norman", "Allen", "Marvin", "Vincent", "Glenn", "Jeffery", "Travis", "Jeff",
      "Chad", "Jacob", "Lee", "Melvin", "Alfred", "Kyle", "Francis", "Bradley", "Jesus", "Herbert",
      "Frederick", "Ray", "Joel", "Edwin", "Don", "Eddie", "Ricky", "Troy", "Randall", "Barry",
      "Alexander", "Bernard", "Mario", "Leroy", "Francisco", "Marcus", "Micheal", "Theodore",
      "Clifford", "Miguel", "Oscar", "Jay", "Jim", "Tom", "Calvin", "Alex", "Jon", "Ronnie",
      "Bill", "Lloyd", "Tommy", "Leon", "Derek", "Warren", "Darrell", "Jerome", "Floyd", "Leo",
      "Alvin", "Tim", "Wesley", "Gordon", "Dean", "Greg", "Jorge", "Dustin", "Pedro", "Derrick",
      "Dan", "Lewis", "Zachary", "Corey", "Herman", "Maurice", "Vernon", "Roberto", "Clyde",
      "Glen", "Hector", "Shane", "Ricardo", "Sam", "Rick", "Lester", "Brent", "Ramon", "Charlie",
      "Tyler", "Gilbert", "Gene"};
  private static final String[] LAST_NAMES = {
      "Smith", "Johnson", "Williams", "Jones", "Brown", "Davis", "Miller", "Wilson", "Moore",
      "Taylor", "Anderson", "Thomas", "Jackson", "White", "Harris", "Martin", "Thompson", "Garcia",
      "Martinez", "Robinson", "Clark", "Rodriguez", "Lewis", "Lee", "Walker", "Hall", "Allen",
      "Young", "Hernandez", "King", "Wright", "Lopez", "Hill", "Scott", "Green", "Adams", "Baker",
      "Gonzalez", "Nelson", "Carter", "Mitchell", "Perez", "Roberts", "Turner", "Phillips",
      "Campbell", "Parker", "Evans", "Edwards", "Collins", "Stewart", "Sanchez", "Morris",
      "Rogers", "Reed", "Cook", "Morgan", "Bell", "Murphy", "Bailey", "Rivera", "Cooper",
      "Richardson", "Cox", "Howard", "Ward", "Torres", "Peterson", "Gray", "Ramirez", "James",
      "Watson", "Brooks", "Kelly", "Sanders", "Price", "Bennett", "Wood", "Barnes", "Ross",
      "Henderson", "Coleman", "Jenkins", "Perry", "Powell", "Long", "Patterson", "Hughes",
      "Flores", "Washington", "Butler", "Simmons", "Foster", "Gonzales", "Bryant", "Alexander",
      "Russell", "Griffin", "Diaz", "Hayes", "Myers", "Ford", "Hamilton", "Graham", "Sullivan",
      "Wallace", "Woods", "Cole", "West", "Jordan", "Owens", "Reynolds", "Fisher", "Ellis",
      "Harrison", "Gibson", "Mcdonald", "Cruz", "Marshall", "Ortiz", "Gomez", "Murray", "Freeman",
      "Wells", "Webb", "Simpson", "Stevens", "Tucker", "Porter", "Hunter", "Hicks", "Crawford",
      "Henry", "Boyd", "Mason", "Morales", "Kennedy", "Warren", "Dixon", "Ramos", "Reyes", "Burns",
      "Gordon", "Shaw", "Holmes", "Rice", "Robertson", "Hunt", "Black", "Daniels", "Palmer",
      "Mills", "Nichols", "Grant", "Knight", "Ferguson", "Rose", "Stone", "Hawkins", "Dunn",
      "Perkins", "Hudson", "Spencer", "Gardner", "Stephens", "Payne", "Pierce", "Berry",
      "Matthews", "Arnold", "Wagner", "Willis", "Ray", "Watkins", "Olson", "Carroll", "Duncan",
      "Snyder", "Hart", "Cunningham", "Bradley", "Lane", "Andrews", "Ruiz", "Harper", "Fox",
      "Riley", "Armstrong", "Carpenter", "Weaver", "Greene", "Lawrence", "Elliott", "Chavez",
      "Sims", "Austin", "Peters", "Kelley", "Franklin", "Lawson"};
  private static final String[] STREET_NAMES =
      {
          "Peachtree", "First", "Second", "Third", "Fourth", "Fifth", "Sixth", "Tenth",
          "Fourteenth", "Spring", "Techwood", "West Peachtree", "Juniper", "Cypress", "Fowler",
          "Piedmont", "Juniper", "Main", "Central", "Currier", "Courtland", "Williams",
          "Centennial", "Olympic", "Baker", "Highland", "Pryor", "Decatur", "Bell", "Edgewood",
          "Mitchell", "Forsyth", "Capital"};
  private static final String[] STREET_SUFFIX = {
      "St", "Rd", "Ln", "Blvd", "Way", "Pkwy", "Cir", "Ave"};

  /**
   * The singleton instance of the database.
   */
  private static ContactDatabase instance;

  /**
   * Get the singleton instance of the contact database.
   * 
   * @return the singleton instance
   */
  public static ContactDatabase get() {
    if (instance == null) {
      instance = new ContactDatabase();
    }
    return instance;
  }

  /**
   * The provider that holds the list of contacts in the database.
   */
  private ListDataProvider<TableModel> dataProvider = new ListDataProvider<TableModel>();

  private final String[] categories = new String[] {"One", "Two", "Three"};

  /**
   * The map of contacts to her friends.
   */
  private final Map<Integer, Set<TableModel>> friendsMap =
      new HashMap<Integer, Set<TableModel>>();

  /**
   * Construct a new contact database.
   */
  private ContactDatabase() {

    // Generate initial data.
    //generateContacts(250);
    generateContacts(4);
  }

  /**
   * Add a new contact.
   * 
   * @param contact the contact to add.
   */
  public void addContact(TableModel contact) {
    List<TableModel> contacts = dataProvider.getList();
    // Remove the contact first so we don't add a duplicate.
    contacts.remove(contact);
    contacts.add(contact);
  }

  /**
   * Add a display to the database. The current range of interest of the display
   * will be populated with data.
   * 
   * @param display a {@Link HasData}.
   */
  public void addDataDisplay(HasData<TableModel> display) {
    dataProvider.addDataDisplay(display);
  }

  /**
   * Generate the specified number of contacts and add them to the data
   * provider.
   * 
   * @param count the number of contacts to generate.
   */
  public void generateContacts(int count) {
    List<TableModel> contacts = dataProvider.getList();
    for (int i = 0; i < count; i++) {
      //contacts.add(createTableModel());
    	contacts.addAll(createDrugModels(nextValue(DRUG1_NAMES), nextValue(DRUG2_NAMES)));
    }
  }


public ListDataProvider<TableModel> getDataProvider() {
    return dataProvider;
  }

  /**
   * Get the categories in the database.
   * 
   * @return the categories in the database
   */
  public String[] queryCategories() {
    return categories;
  }

  /**
   * Query all contacts for the specified category.
   * 
   * @param category the category
   * @return the list of contacts in the category
   */
  public List<TableModel> queryContactsByCategory(String category) {
    List<TableModel> matches = new ArrayList<TableModel>();
    for (TableModel contact : dataProvider.getList()) {
      if (contact.get("Category") == category) {
        matches.add(contact);
      }
    }
    return matches;
  }

  /**
   * Query all contacts for the specified category that begin with the specified
   * first name prefix.
   * 
   * @param category the category
   * @param firstNamePrefix the prefix of the first name
   * @return the list of contacts in the category
   */
  public List<TableModel> queryContactsByCategoryAndFirstName(String category,
      String firstNamePrefix) {
    List<TableModel> matches = new ArrayList<TableModel>();
    for (TableModel contact : dataProvider.getList()) {
      if (contact.get("Category") == category && contact.get("FirstName").startsWith(firstNamePrefix)) {
        matches.add(contact);
      }
    }
    return matches;
  }


  /**
   * Refresh all displays.
   */
  public void refreshDisplays() {
    dataProvider.refresh();
  }

  /**
   * Create a new random {@link TableModel}.
   * 
   * @return the new {@link TableModel}.
   */
  @SuppressWarnings("deprecation")
  private TableModel createTableModel() {
    TableModel contact = new TableModel();
    contact.put("Category", nextValue(categories));
    contact.put("LastName", nextValue(LAST_NAMES));
    if (Random.nextBoolean()) {
      // Male.
      contact.put("FirstName", nextValue(MALE_FIRST_NAMES));
    } else {
      // Female.
      contact.put("FirstName", nextValue(FEMALE_FIRST_NAMES));
    }

    // Create a birthday between 20-80 years ago. or make it null
    int year = (new Date()).getYear() - 21 - Random.nextInt(61);
    String bday = Random.nextInt()%2==0 ? null : String.valueOf(new Date(year, Random.nextInt(12), 1 + Random.nextInt(31)).getTime());
    contact.put("Birthday", bday);
    
    contact.put("BMI", "12.5");
    contact.put("IsAlive", "true");
//    contact.put("Plot", "f898732a0-filehandleid");
    contact.put("Plot", "http://localhost:8089/tomcat-power.gif"); 
    
    // Create an address.
    int addrNum = 1 + Random.nextInt(999);
    String addrStreet = nextValue(STREET_NAMES);
    String addrSuffix = nextValue(STREET_SUFFIX);
    contact.put("Address", (addrNum + " " + addrStreet + " " + addrSuffix));
    return contact;
  }

  private Collection<? extends TableModel> createDrugModels(String drug1, String drug2) {
	  List<TableModel> rows = new ArrayList<TableModel>();

	  for(double drug2conc : new Double[] { 0.266, 0.133, 0.066, 0.033 }) {
		  for(double drug1conc : new Double[] { 7.0, 3.5, 1.75, 0.875 }) {
			  TableModel model = new TableModel();
			  model.put("cellLine", "MCF7");
			  model.put("Drug1", drug1);
			  model.put("Drug1_Conc", String.valueOf(drug1conc));
			  model.put("Drug1_InhibitionMean", String.valueOf(Random.nextDouble()));
			  model.put("Drug1_InhibitionStdev", String.valueOf(Random.nextDouble()));
			  model.put("Drug2", drug2);
			  model.put("Drug2_Conc", String.valueOf(drug2conc));
			  model.put("Drug2_InhibitionMean", String.valueOf(Random.nextDouble()));
			  model.put("Drug2_InhibitionStdev", String.valueOf(Random.nextDouble()));
			  rows.add(model);		  
		  }
	  }
	  
	  return rows;
  }

  
  /**
   * Get the next random value from an array.
   * 
   * @param array the array
   * @return a random value in the array
   */
  private <T> T nextValue(T[] array) {
    return array[Random.nextInt(array.length)];
  }

}
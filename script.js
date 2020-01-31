
// Prefilled example
var data = [
  {description: "Water Bill", price: 100, date: "2019-12-09", category: "Utilities"}
];

// Variables for the different input items on the screen
var descriptionField = document.querySelector("#input-description");
var priceField = document.querySelector("#input-price");
var dateField = document.querySelector("#input-date");
var addButton = document.querySelector("#add-item");
var generateCategoryButton = document.querySelector("#generateCategory");
var generateDateButton = document.querySelector("#generateDate");

// Handles the button events
addButton.addEventListener("click", pushData);
generateCategoryButton.addEventListener("click", updateCategoryGraph);
generateDateButton.addEventListener("click", updateDateGraph);

// Update the list of data
function pushData() {
  var displayedData = document.querySelectorAll("li");
  var datalist = document.querySelector("#data-list");
  
  // Checks to see that the fields aren't empty and adds items to a list
  var dataDescription = document.querySelector("#input-description").value;
  var dataPrice = document.querySelector("#input-price").value;
  var dataDate = document.querySelector("#input-date").value;
  var dataCategory = document.querySelector("#input-category").value;
  if(dataDescription !== "" && dataPrice > 0) {
    data.push({description: dataDescription, price: dataPrice, date: dataDate, category: dataCategory});
  }

  // Removing list items
  for(var i = 0; i < displayedData.length; i++) {
    datalist.removeChild(displayedData[i]);
  }

  // For each piece of data...
  data.forEach(function(item, index){

    // Create a list element with the appropriate information
    var listElement = document.createElement("li");
    listElement.innerHTML = item.description + " | $" + item.price.toString() + " | " + item.date + " | " + item.category;
    document.querySelector("#data-list").appendChild(listElement);

    // Create a span element for a remove box
    var span = document.createElement("span");
    span.setAttribute("dataIndex", index);
    span.innerHTML = "\u00D7";
    span.className = "close";
    listElement.appendChild(span);

    // Add an event listener for removing items
    span.addEventListener("click", function() {
      var dataIndex = this.getAttribute("dataIndex");
      data.splice(dataIndex,1);
      pushData();
    })
  });

  // Clear input fields
  document.querySelector("#input-description").value = null;
  document.querySelector("#input-price").value = null;
  document.querySelector("#input-date").value = null;
  document.querySelector("#input-category").value = null;
}

// Variables for graph creation and sizing
var width = window.innerWidth;
var height = window.innerHeight;
var margin = {top: 100, left: 100, right: 100, bottom: 100};

// Create and update the graph for spending per category
function updateCategoryGraph() {
  d3.select("#graph1").html("");
  
  var categories = data.map(function(d) { return d.category; });
  // Creates a nested array of our data to organize total spending by category
  var sumPerCategory = d3.nest()
    .key(function(d) { return d.category; })
    .rollup(function(v) { return d3.sum(v, function(d) { return d.price; }); })
    .entries(data);
  var maxAmt = d3.max(sumPerCategory, function(d) { return d.value;} );

  var svg = d3.select("#graph1")
    .append("svg")
    .attr("width",width)
    .attr("height", height);

  var yScale = d3.scaleLinear()
    .domain([0, maxAmt])
    .range([height-margin.bottom, margin.top]);

  var xScale = d3.scaleBand()
    .domain(categories)
    .rangeRound([margin.left, width-margin.right])
    .padding(0.5);

  var xAxis = svg.append("g")
    .attr("class","axis")
    .attr("transform",`translate(0, ${height-margin.bottom})`)
    .call(d3.axisBottom().scale(xScale));

  var yAxis = svg.append("g")
    .attr("class","axis")
    .attr("transform",`translate(${margin.left},0)`)
    .call(d3.axisLeft().scale(yScale));

  var bar = svg.selectAll("rect")
    .data(sumPerCategory)
    .enter()
    .append("rect")
      .attr("x", function(d) { return xScale(d.key); })
      .attr("y", function(d) { return yScale(d.value); })
      .attr("width", xScale.bandwidth())
      .attr("height", function(d) { return height - margin.bottom - yScale( d.value ); })
      .attr("fill", "#6b907a");

  var xAxisLabel = svg.append("text")
    .attr("class", "label")
    .attr("x", width / 2)
    .attr("y", height - 50)
    .attr("text-anchor", "middle")
    .text("Categories of Spending");

  var yAxisLabel = svg.append("text")
    .attr("class", "label")
    .attr("transform", "rotate(-90)")
    .attr("x", - height / 2)
    .attr("y", margin.left / 2)
    .attr("text-anchor", "middle")
    .text("Spending Amount ($)");
}

// Create and update the graph for spending per category
function updateDateGraph() {
  d3.select("#graph2").html("");

  var categories = data.map(function(d) { return d.date; });
  // Creates a nested array of our data to organize total spending by date
  var sumPerDate = d3.nest()
    .key(function(d) { return d.date; })
    .rollup(function(v) { return d3.sum(v, function(d) { return d.price; }); })
    .entries(data);
  var maxAmt = d3.max(sumPerDate, function(d) { return d.value;} );

  var svg = d3.select("#graph2")
    .append("svg")
    .attr("width",width)
    .attr("height", height);

  var yScale = d3.scaleLinear()
    .domain([0, maxAmt])
    .range([height-margin.bottom, margin.top]);

  var xScale = d3.scaleBand()
    .domain(categories)
    .rangeRound([margin.left, width-margin.right])
    .padding(0.5);

  var xAxis = svg.append("g")
    .attr("class","axis")
    .attr("transform",`translate(0, ${height-margin.bottom})`)
    .call(d3.axisBottom().scale(xScale));

  var yAxis = svg.append("g")
    .attr("class","axis")
    .attr("transform",`translate(${margin.left},0)`)
    .call(d3.axisLeft().scale(yScale));

  var bar = svg.selectAll("rect")
    .data(sumPerDate)
    .enter()
    .append("rect")
      .attr("x", function(d) { return xScale(d.key); })
      .attr("y", function(d) { return yScale(d.value); })
      .attr("width", xScale.bandwidth())
      .attr("height", function(d) { return height - margin.bottom - yScale(d.value); })
      .attr("fill", "#6b907a");

  var xAxisLabel = svg.append("text")
    .attr("class", "label")
    .attr("x", width / 2)
    .attr("y", height - 50)
    .attr("text-anchor", "middle")
    .text("Purchase Dates");

  var yAxisLabel = svg.append("text")
    .attr("class", "label")
    .attr("transform", "rotate(-90)")
    .attr("x", - height / 2)
    .attr("y", margin.left / 2)
    .attr("text-anchor", "middle")
    .text("Spending Amount ($)");
}

// Load the page
pushData();

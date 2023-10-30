// // TODO: onload function should retrieve the data needed to populate the UI

function add_record(testFile){
  const { file, spamProbability, actualClass } = testFile;
  const fileData = wrap("td", `${file}`);
  const spamData = wrap("td", `${spamProbability}`);
  const classData = wrap("td", `${actualClass}`);
  const row = wrap("tr", fileData + spamData + classData);

  document.getElementById("chart").getElementsByTagName("tbody")[0].innerHTML +=
    row;
}

function wrap(tag, content) {
  return `<${tag}>${content}</${tag}>`;
}

const URL="http://localhost:8080/spamDetector-1.0/api/spam";

var test_data = null;

(function (){
  fetch(URL)
    .then(res=>res.json())
    .then((data)=>{
      console.log(`Loaded data from ${URL}:`,data);
      for (const testFile of data){
        add_record(testFile);
      }
    })
    .catch((err)=>{
      console.log("something went wrong: " + err);
    });
})();

function add_accuracy(data){
  document.getElementById("accuracy").innerHTML=data;
}
function add_precicion(data){
  document.getElementById("precision").innerHTML=data;
}

const URL1="http://localhost:8080/spamDetector-1.0/api/spam/accuracy";
(function (){
  fetch(URL1)
    .then(res=>res.json())
    .then((data)=>{
      add_accuracy(data);
      // test_data=data;
      console.log(`Loaded data from ${URL}:`,data);
    })
    .catch((err)=>{
      console.log("something went wrong: " + err);
    });
})();
const URL2="http://localhost:8080/spamDetector-1.0/api/spam/precision";
(function (){
  fetch(URL2)
    .then(res=>res.json())
    .then((data)=>{
      add_precicion(data);
      test_data=data;
      console.log(`Loaded data from ${URL}:`,data);
    })
    .catch((err)=>{
      console.log("something went wrong: " + err);
    });
})();


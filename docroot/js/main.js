    function start() {  
        
        window['progress'] = setInterval(function() {  
            var pbClient = PF('pbClient');  
              
            updateProgress();  
  
            if(pbClient.getValue() === 100) {  
                clearInterval(window['progress']);
                uploadCompleted();
            }  
  
        }, 1000);  
    }  
  
    function cancel() {  
        clearInterval(window['progress']);  
        PF('pbClient').setValue(0);  
    }  
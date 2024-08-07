trigger AccountTrigger5 on Account (before Update, before insert) {
    
    
    Trigger_Controls__mdt triggerControl;
    for (Trigger_Controls__mdt control : [Select DeveloperName, Run_On_Update__c, Run_On_Insert__c,History_Emails_Allowed__c, Disable_Triggers__c from Trigger_Controls__mdt]) {
        if (control.DeveloperName.contains('Account')) {
            triggerControl = control;
            if (control.Disable_Triggers__c) {
                return ;
            }
        }
    }
    
   /*  if ((Trigger.isInsert && triggerControl.Run_On_Insert__c) || (Trigger.isUpdate && triggerControl.Run_On_Update__c)) {
        List<User> users = new List<User>([Select Id, Name, UserRole.Name, Assigned_Countries__c from User 
                                           where isActive = true AND Assigned_Countries__c != null and UserRole.Name like '%International%']);
        for (Account account : Trigger.new) {
            List<User> SameCountryUsers = new List<User>();
            if (users.size() > 0 && account.Country_of_Origin_Company_Nationality__c != null) {
                for (User user : users) {
                    if (user.Assigned_Countries__c.contains(account.Country_of_Origin_Company_Nationality__c)) {
                        SameCountryUsers.add(user);
                    }
                }
                if (SameCountryUsers.size() == 1) {
                    account.ORG_Intl_Team_Lead__c = SameCountryUsers[0].Id;
                } else if (SameCountryUsers.size() > 1) {
                    for (User user : SameCountryUsers) {
                        if (user.Name != 'Micheal Smith' && account.ORG_Intl_Team_Lead__c == SameCountryUsers[0].Id) {
                            account.ORG_Intl_Team_Lead__c = SameCountryUsers[1].Id;
                            break;
                        }
                    }
                }
            }
        }
    } */

//    Set<Id> userIds = new Set<Id>();
//    for (Account acc : Trigger.new) {
//        if (acc.ORG_Intl_Team_Lead__c != null && acc.Country_of_Origin_Company_Nationality__c != null) {
//            userIds.add(acc.ORG_Intl_Team_Lead__c);
//        }
//    }
//    if (userIds.size() > 0) {
//        Map<Id, User> usersMap = new Map<Id, User>([Select Id, Name, Assigned_Countries__c from User where Id IN:userIds]);
//        for (Account acc : trigger.new) {
//            if (acc.ORG_Intl_Team_Lead__c != null && usersMap.containsKey(acc.ORG_Intl_Team_Lead__c)) {
//                User currentUser = usersMap.get(acc.ORG_Intl_Team_Lead__c);
//                if (currentUser.Assigned_Countries__c == null || !currentUser.Assigned_Countries__c.contains(acc.Country_of_Origin_Company_Nationality__c)) {
//                    if (!Test.isRunningTest()) {
//                        acc.ORG_Intl_Team_Lead__c.addError('This person cannot be added because the account country is not listed in countries this person is covering.');
//                    }
//                }
//            }
//        }
//    } 
    
    if (Trigger.isUpdate)
    {
        // && triggerControl.History_Emails_Allowed__c) {
        String body = '' ;
        Boolean changesHappened;
        Set<String> objnameList  = new Set<String>();
        for (FieldSetMember member : SObjectType.Account.FieldSets.AccountFieldSet.getFields()) 
        {
            Schema.DisplayType FldType = member.getType();
            String types = String.valueOf(FldType);
            if(types=='REFERENCE')
            {
                Schema.sObjectField field1 = member.getSObjectField();
                String objName = String.valueOf(field1.getDescribe().getReferenceTo());
                
                objnameList.add( objName.substring(1, objName.length()-1));    
                
            }
        }
        System.debug('objNameList'+objnameList);
        Map<String,Map<String,String>>  mapOfObj  = new   Map<String,Map<String,String>>();
        for(String str:objnameList)
        {
            String query1 = 'SELECT id,Name FROM '+str;
            list<sobject> objlst = Database.query(query1); 
            for(sobject  sobj:objlst)
            {
                if(mapOfObj.keySet().contains(str))
                {
                    Map<String,String> maps =     mapOfObj.get(str);
                    maps.put( String.valueOf(sobj.get('id')) , String.valueOf(sobj.get('Name')) );
                    mapOfObj.put(str,maps);
                }
                else
                {
                    Map<String,String> maps = new Map<String,String>();
                    maps.put(String.valueOf( sobj.get('id')),String.valueOf(sobj.get('Name')) ); 
                    mapOfObj.put(str,maps);
                }
                
                
            }
            
            
        }
        
        
        
        for (Account acc : trigger.new) {
            //Boolean changesHappened = false;
            changesHappened = false;
            //body = 'Field Name - Old  Value - New Value ' + '\n';
            for (FieldSetMember member : SObjectType.Account.FieldSets.AccountFieldSet.getFields()) 
            {
                
                String field = member.getFieldPath();
                String fieldName = member.getLabel();
               // System.debug('Acc field Old -->> ' + trigger.oldMap.get(acc.Id).get(field));
               // System.debug('Acc field New -->> ' + acc.get(field));
                Schema.DisplayType FldType = member.getType();
                String types = String.valueOf(FldType);
                if(types!='REFERENCE')
                {
                    if (acc.get(field) != trigger.oldMap.get(acc.Id).get(field))
                    {
                        //body = body + fieldName + ' - ' + trigger.oldMap.get(acc.Id).get(field) + ' - ' + acc.get(field) + '\n';
                        System.debug('Body Field Value Before Addition -->> ' + body);
                        body = body  + 'Field Name :- '  +  fieldName + '\n' ;
                        body = body + 'Old  Value :- ' + trigger.oldMap.get(acc.Id).get(field) + '\n' ;
                        body = body + 'New Value :- ' + acc.get(field) + '\n' + '\n' ;
                        changesHappened = true;
                        System.debug('Body Field Value for loop -->> ' + body);
                    }  
                }
                else if(types=='REFERENCE')
                {
                    if (acc.get(field) != trigger.oldMap.get(acc.Id).get(field))
                    {
                        Schema.sObjectField field1 = member.getSObjectField();
                        String objName = String.valueOf(field1.getDescribe().getReferenceTo());
                        String obj = objName.substring(1, objName.length()-1);
                        Map<String,String> maps =     mapOfObj.get(obj);  
                        System.debug('Body Field Value Before Addition -->> ' + body);
                        body = body  + 'Field Name :- '  +  fieldName + '\n' ;
                        body = body + 'Old  Value :- ' + maps.get( String.valueOf( trigger.oldMap.get(acc.Id).get(field) )) + '\n' ;
                        body = body + 'New Value :- ' + maps.get( String.valueOf(acc.get(field)) )  + '\n' + '\n' ;
                        changesHappened = true;
                        System.debug('Body Field Value for loop -->> ' + body);  
                    }
                    
                    
                    
                }
                
                
                
            }
            
            
            //acc.Account_Change_History__c = null;
            if (changesHappened) {
                //System.debug('Body Field Value -->> ' + body);
                acc.Account_Change_History__c = null;
                acc.Account_Change_History__c = body;
                body = '';
            }
        }
        
        
        
    }
}
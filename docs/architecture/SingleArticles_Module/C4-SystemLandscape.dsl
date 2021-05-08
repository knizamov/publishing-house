workspace "Publishing House" {

    model {
        chiefEditor = person "Editor Chief" "Manages magazines' editions and topics"
        journalist = person "Journalist" "Responsible for drafting and publishing articles for a magazine"
        copywriter = person "Copywriter" "Reviews articles and their content, suggest changes to articles"

        magazines = softwareSystem "Magazines" "Maintains magazine catalog"
        articles = softwareSystem "Articles" "Supports drafting, reviewing and publishing articles for a magazine"
        users = softwareSystem "Users" "Responsible for authentication, authorization and user profiles"

        // Actor interactions
        chiefEditor -> magazines "Creates new magazine editions, adds topics"
        journalist -> magazines  "Views magazine topics"
        journalist -> articles "Submits draft articles, edit drafts, applies change suggestions, publishes articles"
        copywriter -> articles "Views draft articles, suggests changes to draft articles, resolves change suggestions"

        // Software system interactions
        magazines -> users "Gets user details (roles)" "" "auth"
        articles -> users "Gets user details (roles)" "" "auth"

    }

   views {
       systemlandscape "SystemLandscape" {
           include *
           autolayout
       }

       styles {
           element "Software System" {
               background #1168bd
               color #ffffff
           }
           element "Person" {
               shape person
               background #08427b
               color #ffffff
           }

           relationship "auth" {
               position 70
           }

           relationship "Relationship" {
               dashed false
           }

           relationship "event" {
               dashed true
           }

           relationship "position20" {
               position 20
           }

           relationship "curved" {
               routing Curved
           }
       }
   }

}
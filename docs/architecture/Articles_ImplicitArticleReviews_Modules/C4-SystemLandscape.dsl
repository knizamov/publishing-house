workspace "Publishing House" {

    model {
        chiefEditor = person "Editor Chief" "Manages magazines' editions and topics"
        journalist = person "Journalist" "Responsible for drafting and publishing articles for a magazine"
        copywriter = person "Copywriter" "Reviews articles and their content, suggest changes to articles"

        magazines = softwareSystem "Magazines" "Maintains magazine catalog"
        articles = softwareSystem "Articles" "Supports drafting and publishing articles for a magazine"
        contentReviews = softwareSystem "Content/Article Reviews" "Supports content reviews of an article (or possibly some other type of content)"
        users = softwareSystem "Users" "Responsible for authentication, authorization and user profiles"



        // Actor interactions
        chiefEditor -> magazines "Creates new magazine editions, adds topics"
        journalist -> magazines  "Views magazine topics"
        journalist -> articles "Submits draft articles, edit drafts, publishes articles"
        journalist -> contentReviews "Views change suggestions, marks change suggestions as applied" "" "position20, curved"
        copywriter -> contentReviews "Suggests changes to draft articles, resolves change suggestions"
        copywriter -> articles "Views draft articles"

        // Software system interactions
        articles -> contentReviews "Notifies Article Draft Created/Edited/Published" "event" "event"
        articles -> contentReviews "Checks review status"

        magazines -> users "Gets user details (roles)" "" "auth"
        articles -> users "Gets user details (roles)" "" "auth"
        contentReviews -> users "Gets user details (roles)" "" "auth"

    }

    views {
        systemlandscape "SystemLandscape" {
            include *
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
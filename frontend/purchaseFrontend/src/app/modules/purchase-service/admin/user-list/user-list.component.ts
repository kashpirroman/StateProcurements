import { Component, OnInit } from '@angular/core';
import {User} from "../../../../models/Users";
import {UserService} from "../../../../services/user.service";


@Component({
  selector: 'app-user-list',
  templateUrl: './user-list.component.html',
  styleUrls: ['./user-list.component.css']
})
export class UserListComponent implements OnInit {
  users: User[];

  constructor(userService:UserService) { }

  ngOnInit(): void {
    this.users
  }

}
